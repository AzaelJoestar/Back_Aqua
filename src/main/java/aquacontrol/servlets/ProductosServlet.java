package aquacontrol.servlets;

import aquacontrol.dao.ProductoDao;
import aquacontrol.modelos.Producto;
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
 * API REST de la tabla productos: /api/productos
 *   GET    /api/productos        lista            GET    /api/productos/{id}  uno
 *   POST   /api/productos        crea (201)       PUT    /api/productos/{id}  reemplaza
 *   PATCH  /api/productos/{id}   cambio parcial   DELETE /api/productos/{id}  elimina (204)
 */
@WebServlet(name = "ProductosServlet", urlPatterns = "/api/productos/*")
public class ProductosServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProductoDao dao = new ProductoDao();

    /** HttpServlet no tiene doPatch: se detecta aquí y se manda a doPatch. */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, res);
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
                List<Producto> lista = dao.listar();
                Respuesta.json(res, 200, lista);
            } else {
                Producto registro = dao.buscarPorId(id);
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
            Producto nuevo = Json.desde(Json.leerCuerpo(req), Producto.class);
            Respuesta.json(res, 201, dao.insertar(nuevo));
        });
    }

    /** PUT: reemplaza el registro completo. */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            int id = HttpUtil.idObligatorio(req);
            Producto nuevo = Json.desde(Json.leerCuerpo(req), Producto.class);
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
