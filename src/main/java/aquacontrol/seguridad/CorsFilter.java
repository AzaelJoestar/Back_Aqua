package aquacontrol.seguridad;

import aquacontrol.config.Config;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/** Permite que el front (Angular, en otro origen) llame a la API. Solo los orígenes de cors.origen. */
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse res = (HttpServletResponse) rs;

        String origen = req.getHeader("Origin");
        if (origen != null && permitido(origen)) {
            res.setHeader("Access-Control-Allow-Origin", origen);
            res.setHeader("Vary", "Origin");
            res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUSH, PUT, PATCH, DELETE, OPTIONS");
            res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
            res.setHeader("Access-Control-Max-Age", "3600");
        }
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        chain.doFilter(rq, rs);
    }

    private boolean permitido(String origen) {
        // Sin valor por defecto: si cors.origen no está configurado, no se permite ningún origen.
        return Arrays.stream(Config.get("cors.origen", "").split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .anyMatch(origen::equals);
    }
}
