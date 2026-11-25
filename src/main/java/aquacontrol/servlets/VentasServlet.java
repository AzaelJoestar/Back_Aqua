package aquacontrol.servlets;

import aquacontrol.dao.VentaDao;
import aquacontrol.modelos.Venta;
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
 * API REST de ventas: /api/ventas (protegida con JWT).
 * POST registra, PUT/PATCH editan (el stock se devuelve y se vuelve a descontar en la misma transacción)
 * y DELETE cancela la venta devolviendo el stock.
 *
 * POST body: { "idCliente": 1, "detalles": [ { "idProducto": 1, "cantidad": 2 } ] }
 */
@WebServlet(name = "VentasServlet", urlPatterns = "/api/ventas/*")
public class VentasServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final VentaDao dao = new VentaDao();

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

    /**
     * PUSH: método propio del proyecto (no es parte del estándar HTTP ni de la API de Servlets).
     * Envía un registro nuevo al servidor, con la misma lógica que POST. No es el Server Push de HTTP/2.
     */
    protected void doPush(HttpServletRequest req, HttpServletResponse res) throws IOException {
        doPost(req, res);
    }

    /** PUT: reemplaza el cliente y los productos de la venta (devuelve y vuelve a descontar el stock). */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            int id = HttpUtil.idObligatorio(req);
            Venta peticion = Json.desde(Json.leerCuerpo(req), Venta.class);
            Venta guardada = dao.reemplazar(id, peticion);
            if (guardada == null) throw new ApiException(404, "Venta no encontrada");
            Respuesta.json(res, 200, guardada);
        });
    }

    /**
     * PATCH: cambio parcial. Solo se pueden modificar "idCliente" y "detalles"
     * (el total, la fecha y el vendedor nunca se aceptan del cliente).
     */
    protected void doPatch(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpUtil.ejecutar(res, () -> {
            int id = HttpUtil.idObligatorio(req);
            String cuerpo = Json.leerCuerpo(req);
            JsonObject cambios = Json.objeto(cuerpo);
            if (!cambios.has("idCliente") && !cambios.has("detalles"))
                throw new ApiException(400, "Solo se pueden modificar idCliente y detalles");

            Venta guardada;
            if (cambios.has("detalles")) {
                Venta peticion = Json.desde(cuerpo, Venta.class);
                if (!cambios.has("idCliente")) { // sin idCliente en el cuerpo se conserva el actual
                    Venta actual = dao.buscarPorId(id);
                    if (actual == null) throw new ApiException(404, "Venta no encontrada");
                    peticion.setIdCliente(actual.getIdCliente());
                }
                guardada = dao.reemplazar(id, peticion);
            } else {
                JsonObject soloCliente = new JsonObject();
                soloCliente.add("idCliente", cambios.get("idCliente"));
                guardada = dao.parchar(id, soloCliente) ? dao.buscarPorId(id) : null;
            }
            if (guardada == null) throw new ApiException(404, "Venta no encontrada");
            Respuesta.json(res, 200, guardada);
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
