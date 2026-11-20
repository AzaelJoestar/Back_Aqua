package aquacontrol.servlets;

import aquacontrol.dao.VentaDao;
import aquacontrol.modelos.Venta;
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
import java.util.List;

/**
 * API REST de ventas: /api/ventas (protegida con JWT).
 * Una venta no se edita (afecta stock y totales): se registra con POST o se cancela con DELETE.
 *
 * POST body: { "idCliente": 1, "detalles": [ { "idProducto": 1, "cantidad": 2 } ] }
 */
@WebServlet(name = "VentasServlet", urlPatterns = "/api/ventas/*")
public class VentasServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final VentaDao dao = new VentaDao();

    /** HttpServlet no tiene doPatch: se detecta aquí y se manda a doPatch. */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            doPatch(req, res);
        } else {
            super.service(req, res);
        }
    }

    /** GET: sin id lista todas; con id devuelve una venta con sus detalles. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            Integer id = HttpUtil.idDeRuta(req);
            if (id == null) {
                List<Venta> lista = dao.listar();
                Respuesta.json(res, 200, lista);
            } else {
                Venta v = dao.buscarPorId(id);
                if (v == null) throw new ApiException(404, "Venta no encontrada");
                Respuesta.json(res, 200, v);
            }
        });
    }

    /** POST: registra la venta completa en una transacción (venta + detalles + stock + movimientos). */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            if (HttpUtil.idDeRuta(req) != null) throw new ApiException(405, "Para registrar una venta usa POST sin id");
            Venta peticion = Json.desde(Json.leerCuerpo(req), Venta.class);
            Object quien = req.getAttribute("idUsuario");
            if (quien == null) throw new ApiException(401, "Se requiere iniciar sesión");
            // El vendedor sale del token, no del cuerpo de la petición.
            Respuesta.json(res, 201, dao.registrar(peticion, (Integer) quien));
        });
    }

    /** PUT: no permitido; una venta no se edita. */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            throw new ApiException(405, "Las ventas no se editan: cancela la venta y registra una nueva");
        });
    }

    /** PATCH: no permitido; una venta no se edita. */
    protected void doPatch(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            throw new ApiException(405, "Las ventas no se editan: cancela la venta y registra una nueva");
        });
    }

    /** DELETE: cancela la venta, devuelve el stock y elimina sus detalles (204). */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            if (!dao.eliminarCompleta(HttpUtil.idObligatorio(req))) throw new ApiException(404, "Venta no encontrada");
            res.setStatus(204);
        });
    }
}
