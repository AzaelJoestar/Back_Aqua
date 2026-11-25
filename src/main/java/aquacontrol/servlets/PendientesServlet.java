package aquacontrol.servlets;

import aquacontrol.dao.PendienteDao;
import aquacontrol.modelos.Pendiente;
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
 * API REST de la tabla pendientes: /api/pendientes
 *   GET    /api/pendientes        lista            GET    /api/pendientes/{id}  uno
 *   POST   /api/pendientes        crea (201)       PUT    /api/pendientes/{id}  reemplaza
 *   PATCH  /api/pendientes/{id}   cambio parcial   DELETE /api/pendientes/{id}  elimina (204)
 */
@WebServlet(name = "PendientesServlet", urlPatterns = "/api/pendientes/*")
public class PendientesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final PendienteDao dao = new PendienteDao();

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

    /** GET: sin id lista todos; con id devuelve uno. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            Integer id = HttpUtil.idDeRuta(req);
            if (id == null) {
                List<Pendiente> lista = dao.listar();
                Respuesta.json(res, 200, lista);
            } else {
                Pendiente registro = dao.buscarPorId(id);
                if (registro == null) throw new ApiException(404, "Registro no encontrado");
                Respuesta.json(res, 200, registro);
            }
        });
    }

    /** POST: crea un registro nuevo con el JSON recibido. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            if (HttpUtil.idDeRuta(req) != null) throw new ApiException(405, "Para crear usa POST sin id");
            Pendiente nuevo = Json.desde(Json.leerCuerpo(req), Pendiente.class);
            Respuesta.json(res, 201, dao.insertar(nuevo));
        });
    }

    /**
     * PUSH: método propio del proyecto (no es parte del estándar HTTP ni de la API de Servlets).
     * Envía un registro nuevo al servidor, con la misma lógica que POST. No es el Server Push de HTTP/2.
     */
    protected void doPush(HttpServletRequest req, HttpServletResponse res) throws IOException {
        doPost(req, res);
    }

    /** PUT: reemplaza el registro completo. */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            int id = HttpUtil.idObligatorio(req);
            Pendiente nuevo = Json.desde(Json.leerCuerpo(req), Pendiente.class);
            if (!dao.actualizar(id, nuevo)) throw new ApiException(404, "Registro no encontrado");
            Respuesta.json(res, 200, dao.buscarPorId(id));
        });
    }

    /** PATCH: modifica solo los campos enviados. */
    protected void doPatch(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            int id = HttpUtil.idObligatorio(req);
            JsonObject cambios = Json.objeto(Json.leerCuerpo(req));
            if (!dao.parchar(id, cambios)) throw new ApiException(404, "Registro no encontrado");
            Respuesta.json(res, 200, dao.buscarPorId(id));
        });
    }

    /** DELETE: elimina el registro (204 sin cuerpo). */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            if (!dao.eliminar(HttpUtil.idObligatorio(req))) throw new ApiException(404, "Registro no encontrado");
            res.setStatus(204);
        });
    }
}
