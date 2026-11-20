package aquacontrol.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Funciones de apoyo para los servlets (NO es un servlet ni se hereda): leer el id de la ruta
 * y convertir cualquier error en una respuesta JSON con el código HTTP adecuado.
 * Cada servlet llama a estos métodos estáticos desde sus propios doGet/doPost/doPut/doPatch/doDelete.
 */
public final class HttpUtil {
    private static final Logger LOG = Logger.getLogger(HttpUtil.class.getName());

    private HttpUtil() {}

    @FunctionalInterface
    public interface Accion {
        void ejecutar() throws Exception;
    }

    /** Id de /api/tabla/{id}; null si la ruta es /api/tabla. */
    public static Integer idDeRuta(HttpServletRequest req) {
        String info = req.getPathInfo();
        if (info == null || info.equals("/")) return null;
        String[] partes = info.substring(1).split("/");
        if (partes.length != 1) throw new ApiException(404, "Ruta no encontrada");
        try {
            return Integer.valueOf(partes[0]);
        } catch (NumberFormatException e) {
            throw new ApiException(400, "El id debe ser un número");
        }
    }

    public static int idObligatorio(HttpServletRequest req) {
        Integer id = idDeRuta(req);
        if (id == null) throw new ApiException(400, "Falta el id en la ruta");
        return id;
    }

    /** Ejecuta la acción y traduce los errores a JSON: 400, 404, 405, 409 o 500. */
    public static void ejecutar(HttpServletResponse res, Accion accion) throws IOException {
        try {
            accion.ejecutar();
        } catch (ApiException e) {
            Respuesta.error(res, e.getEstado(), e.getMessage());
        } catch (SQLException e) {
            String st = e.getSQLState() == null ? "" : e.getSQLState();
            if (st.equals("23505")) Respuesta.error(res, 409, "Ya existe un registro con ese valor único");
            else if (st.equals("23503")) Respuesta.error(res, 409, "La referencia no existe o el registro está en uso por otros");
            else if (st.equals("23502")) Respuesta.error(res, 400, "Falta un campo obligatorio");
            else if (st.startsWith("22") || st.equals("23514")) Respuesta.error(res, 400, "Algún valor tiene un formato inválido");
            else {
                LOG.log(Level.SEVERE, "Error de base de datos", e);
                Respuesta.error(res, 500, "Error interno del servidor");
            }
        } catch (com.google.gson.JsonParseException | IllegalStateException | ClassCastException | NumberFormatException | DateTimeParseException e) {
            Respuesta.error(res, 400, "Datos inválidos en la petición");
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error inesperado", e);
            Respuesta.error(res, 500, "Error interno del servidor");
        }
    }
}
