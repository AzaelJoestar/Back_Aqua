package aquacontrol.util;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public final class Respuesta {
    private Respuesta() {}

    public static void json(HttpServletResponse res, int estado, Object cuerpo) throws IOException {
        res.setStatus(estado);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(Json.gson().toJson(cuerpo));
    }

    public static void error(HttpServletResponse res, int estado, String mensaje) throws IOException {
        json(res, estado, Map.of("error", mensaje));
    }
}
