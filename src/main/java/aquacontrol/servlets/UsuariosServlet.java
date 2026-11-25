package aquacontrol.servlets;

import aquacontrol.dao.UsuarioDao;
import aquacontrol.modelos.Usuario;
import aquacontrol.seguridad.Passwords;
import aquacontrol.util.ApiException;
import aquacontrol.util.HttpUtil;
import aquacontrol.util.Json;
import aquacontrol.util.Respuesta;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * API REST de usuarios: /api/usuarios (protegida con JWT y solo para administrador).
 * La contraseña llega en texto plano UNA vez, se guarda como hash BCrypt y nunca se devuelve.
 */
@WebServlet(name = "UsuariosServlet", urlPatterns = "/api/usuarios/*")
public class UsuariosServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MIN_PASSWORD = 6;
    private final UsuarioDao dao = new UsuarioDao();

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

    /** GET: sin id lista todos; con id devuelve uno. El hash de la contraseña nunca sale. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            Integer id = HttpUtil.idDeRuta(req);
            if (id == null) {
                List<Usuario> lista = dao.listar();
                lista.forEach(u -> u.setPassword(null));
                Respuesta.json(res, 200, lista);
            } else {
                Usuario u = dao.buscarPorId(id);
                if (u == null) throw new ApiException(404, "Registro no encontrado");
                u.setPassword(null);
                Respuesta.json(res, 200, u);
            }
        });
    }

    /** POST: crea un usuario; la contraseña se guarda cifrada con BCrypt. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            if (HttpUtil.idDeRuta(req) != null) throw new ApiException(405, "Para crear usa POST sin id");
            Usuario nuevo = Json.desde(Json.leerCuerpo(req), Usuario.class);
            if (nuevo.getUsuario() == null || nuevo.getUsuario().isBlank()) throw new ApiException(400, "El usuario es obligatorio");
            validar(nuevo.getPassword());
            nuevo.setPassword(Passwords.cifrar(nuevo.getPassword()));
            Usuario creado = dao.insertar(nuevo);
            creado.setPassword(null);
            Respuesta.json(res, 201, creado);
        });
    }

    /**
     * PUSH: método propio del proyecto (no es parte del estándar HTTP ni de la API de Servlets).
     * Envía un registro nuevo al servidor, con la misma lógica que POST. No es el Server Push de HTTP/2.
     */
    protected void doPush(HttpServletRequest req, HttpServletResponse res) throws IOException {
        doPost(req, res);
    }

    /** PUT: reemplaza el usuario. Si no se envía contraseña se conserva la actual. */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            int id = HttpUtil.idObligatorio(req);
            Usuario nuevo = Json.desde(Json.leerCuerpo(req), Usuario.class);
            if (nuevo.getUsuario() == null || nuevo.getUsuario().isBlank()) throw new ApiException(400, "El usuario es obligatorio");
            if (nuevo.getPassword() == null || nuevo.getPassword().isBlank()) {
                Usuario actual = dao.buscarPorId(id);
                if (actual == null) throw new ApiException(404, "Registro no encontrado");
                nuevo.setPassword(actual.getPassword());
            } else {
                validar(nuevo.getPassword());
                nuevo.setPassword(Passwords.cifrar(nuevo.getPassword()));
            }
            if (!dao.actualizar(id, nuevo)) throw new ApiException(404, "Registro no encontrado");
            Usuario guardado = dao.buscarPorId(id);
            guardado.setPassword(null);
            Respuesta.json(res, 200, guardado);
        });
    }

    /** PATCH: modifica solo los campos enviados. Si viene password se cifra; si viene vacía se ignora. */
    protected void doPatch(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            int id = HttpUtil.idObligatorio(req);
            JsonObject cambios = Json.objeto(Json.leerCuerpo(req));
            if (cambios.has("password")) {
                String nueva = cambios.get("password").isJsonNull() ? null : cambios.get("password").getAsString();
                if (nueva == null || nueva.isBlank()) {
                    cambios.remove("password");
                } else {
                    validar(nueva);
                    cambios.addProperty("password", Passwords.cifrar(nueva));
                }
            }
            if (!dao.parchar(id, cambios)) throw new ApiException(404, "Registro no encontrado");
            Usuario guardado = dao.buscarPorId(id);
            guardado.setPassword(null);
            Respuesta.json(res, 200, guardado);
        });
    }

    /** DELETE: elimina el usuario (204 sin cuerpo). */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            if (!dao.eliminar(HttpUtil.idObligatorio(req))) throw new ApiException(404, "Registro no encontrado");
            res.setStatus(204);
        });
    }

    private void validar(String password) {
        if (password == null || password.length() < MIN_PASSWORD)
            throw new ApiException(400, "La contraseña debe tener al menos " + MIN_PASSWORD + " caracteres");
    }
}
