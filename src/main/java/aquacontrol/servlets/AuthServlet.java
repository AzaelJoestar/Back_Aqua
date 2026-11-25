package aquacontrol.servlets;

import aquacontrol.dao.RolPermisoDao;
import aquacontrol.dao.UsuarioDao;
import aquacontrol.modelos.RolPermiso;
import aquacontrol.modelos.Usuario;
import aquacontrol.seguridad.Passwords;
import aquacontrol.seguridad.Tokens;
import aquacontrol.seguridad.TokensRevocados;
import aquacontrol.util.ApiException;
import aquacontrol.util.HttpUtil;
import aquacontrol.util.Json;
import aquacontrol.util.Respuesta;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sesión del usuario. Todo viaja por HTTPS (HttpsFilter).
 *   POST   /api/auth/login      { usuario, password }  -> token JWT           (público)
 *   GET    /api/auth/sesion                            -> quién es el dueño del token
 *   PUT    /api/auth/password   { passwordActual, passwordNueva }  -> cambia su contraseña
 *   PATCH  /api/auth/password   igual que PUT (solo se puede modificar la contraseña)
 *   DELETE /api/auth/sesion                            -> cierra la sesión (invalida el token)
 * GET, PUT, PATCH y DELETE exigen el token (AuthFilter, ver web.xml).
 */
@WebServlet(name = "AuthServlet", urlPatterns = "/api/auth/*")
public class AuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MIN_PASSWORD = 6;
    // Se compara contra un hash falso cuando el usuario no existe, para que tarde lo mismo.
    private static final String HASH_FALSO = Passwords.cifrar("sin-usuario");

    private final UsuarioDao usuarios = new UsuarioDao();
    private final RolPermisoDao roles = new RolPermisoDao();

    private static class Credenciales {
        String usuario;
        String password;
    }

    private static class CambioPassword {
        String passwordActual;
        String passwordNueva;
    }

    /** PATCH y PUSH no tienen doXxx estándar en HttpServlet: se detectan aquí en service() y se mandan a doPatch() y doPush(). */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String metodo = req.getMethod();
        if ("PATCH".equalsIgnoreCase(metodo)) {
            doPatch(req, res);
        } else if ("PUSH".equalsIgnoreCase(metodo)) {
            doPush(req, res);
        } else {
            super.service(req, res);
        }
    }

    private static String ruta(HttpServletRequest req) {
        String info = req.getPathInfo();
        return info == null ? "" : info;
    }

    /** GET /api/auth/sesion: datos del usuario dueño del token. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            if (!ruta(req).equals("/sesion")) throw new ApiException(404, "Ruta no encontrada");
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("idUsuario", req.getAttribute("idUsuario"));
            datos.put("usuario", req.getAttribute("usuario"));
            datos.put("rol", req.getAttribute("rol"));
            datos.put("venceEn", String.valueOf(req.getAttribute("venceEn")));
            Respuesta.json(res, 200, datos);
        });
    }

    /** POST /api/auth/login: valida usuario y contraseña (BCrypt) y entrega el token. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            if (!ruta(req).equals("/login")) throw new ApiException(404, "Ruta no encontrada");
            Credenciales cr = Json.desde(Json.leerCuerpo(req), Credenciales.class);
            if (cr.usuario == null || cr.usuario.isBlank() || cr.password == null || cr.password.isEmpty())
                throw new ApiException(400, "Escribe usuario y contraseña");

            Usuario u = usuarios.buscarPorUsuario(cr.usuario.trim());
            boolean correcto;
            if (u == null) {
                Passwords.verificar(cr.password, HASH_FALSO);
                correcto = false;
            } else {
                correcto = Passwords.verificar(cr.password, u.getPassword());
            }
            if (!correcto || !"activo".equalsIgnoreCase(u.getEstado()))
                throw new ApiException(401, "Usuario o contraseña incorrectos");

            // Contraseña vieja en texto plano: se convierte a hash en el primer acceso correcto.
            if (!Passwords.esHash(u.getPassword())) usuarios.actualizarPassword(u.getIdUsuario(), Passwords.cifrar(cr.password));

            RolPermiso rol = u.getIdRol() == null ? null : roles.buscarPorId(u.getIdRol());
            String nombreRol = rol == null || rol.getRolNombre() == null ? "sin-rol" : rol.getRolNombre();

            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("idUsuario", u.getIdUsuario());
            datos.put("nombre", u.getNombre());
            datos.put("apellido", u.getApellido());
            datos.put("usuario", u.getUsuario());
            datos.put("rol", nombreRol);
            datos.put("cambiarPassword", u.getCambiarPassword() != null && u.getCambiarPassword() == 1);

            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("token", Tokens.crear(u.getIdUsuario(), u.getUsuario(), nombreRol));
            cuerpo.put("tipo", "Bearer");
            cuerpo.put("expiraEnSegundos", Tokens.horasDeVida() * 3600);
            cuerpo.put("usuario", datos);
            Respuesta.json(res, 200, cuerpo);
        });
    }

    /**
     * PUSH: método propio del proyecto (no es parte del estándar HTTP ni de la API de Servlets).
     * Envía un registro nuevo al servidor, con la misma lógica que POST. No es el Server Push de HTTP/2.
     */
    protected void doPush(HttpServletRequest req, HttpServletResponse res) throws IOException {
        doPost(req, res);
    }

    /** PUT /api/auth/password: el usuario cambia su propia contraseña. */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        cambiarPassword(req, res);
    }

    /** PATCH /api/auth/password: de la cuenta solo se puede modificar la contraseña, igual que PUT. */
    protected void doPatch(HttpServletRequest req, HttpServletResponse res) throws IOException {
        cambiarPassword(req, res);
    }

    private void cambiarPassword(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            if (!ruta(req).equals("/password")) throw new ApiException(404, "Ruta no encontrada");
            CambioPassword c = Json.desde(Json.leerCuerpo(req), CambioPassword.class);
            if (c.passwordActual == null || c.passwordNueva == null)
                throw new ApiException(400, "Envía passwordActual y passwordNueva");
            if (c.passwordNueva.length() < MIN_PASSWORD)
                throw new ApiException(400, "La contraseña debe tener al menos " + MIN_PASSWORD + " caracteres");

            int id = (Integer) req.getAttribute("idUsuario"); // sale del token, no del cuerpo
            Usuario u = usuarios.buscarPorId(id);
            if (u == null || !Passwords.verificar(c.passwordActual, u.getPassword()))
                throw new ApiException(401, "La contraseña actual es incorrecta");

            usuarios.cambiarPassword(id, Passwords.cifrar(c.passwordNueva));
            Respuesta.json(res, 200, Map.of("mensaje", "Contraseña actualizada"));
        });
    }

    /** DELETE /api/auth/sesion: cierra la sesión; el token deja de servir aunque no haya vencido. */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            if (!ruta(req).equals("/sesion")) throw new ApiException(404, "Ruta no encontrada");
            TokensRevocados.revocar((String) req.getAttribute("jti"), (Instant) req.getAttribute("venceEn"));
            res.setStatus(204);
        });
    }
}
