package aquacontrol.seguridad;

import aquacontrol.config.Config;
import aquacontrol.util.Respuesta;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Ruta específica: la API solo responde por el nombre de host configurado en app.host
 * (ej. aquacontrol.local) y rechaza el acceso por localhost, 127.0.0.1 o cualquier otro nombre.
 * Se puede apagar con host.estricto=false.
 */
public class HostFilter implements Filter {

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse res = (HttpServletResponse) rs;

        String permitidos = Config.get("app.host", "");
        boolean estricto = Boolean.parseBoolean(Config.get("host.estricto", "true"));
        if (!estricto || permitidos.isBlank()) {
            chain.doFilter(rq, rs);
            return;
        }

        String host = req.getServerName(); // solo el nombre, sin puerto
        boolean ok = host != null && Arrays.stream(permitidos.split(","))
                .map(String::trim)
                .anyMatch(host::equalsIgnoreCase);
        if (!ok) {
            Respuesta.error(res, 421, "Host no permitido. La API solo responde en: " + permitidos);
            return;
        }
        chain.doFilter(rq, rs);
    }
}
