package aquacontrol.servlets;

import aquacontrol.dao.RolPermisoDao;
import aquacontrol.dao.UsuarioDao;
import aquacontrol.modelos.RolPermiso;
import aquacontrol.modelos.Usuario;
import aquacontrol.seguridad.Passwords;
import aquacontrol.seguridad.Tokens;
import aquacontrol.util.ApiException;
import aquacontrol.util.Json;
import aquacontrol.util.Respuesta;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inicio de sesión: POST /api/auth/login  { "usuario": "...", "password": "..." }
 * Responde con un token JWT que el front envía como "Authorization: Bearer <token>".
 */
@WebServlet(name = "AuthServlet", urlPatterns = "/api/auth/login")
public class AuthServlet extends HttpServlet {
    private static final Logger LOG = Logger.getLogger(AuthServlet.class.getName());
    // Se compara contra un hash falso cuando el usuario no existe, para que tarde lo mismo.
    private static final String HASH_FALSO = Passwords.cifrar("sin-usuario");

    private final UsuarioDao usuarios = new UsuarioDao();
    private final RolPermisoDao roles = new RolPermisoDao();

    private static class Credenciales {
        String usuario;
        String password;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
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
        } catch (ApiException e) {
            Respuesta.error(res, e.getEstado(), e.getMessage());
        } catch (com.google.gson.JsonParseException e) {
            Respuesta.error(res, 400, "Datos inválidos en la petición");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error en el login", e);
            Respuesta.error(res, 500, "Error interno del servidor");
        }
    }
}
