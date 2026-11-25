package aquacontrol.seguridad;

import aquacontrol.config.Config;
import aquacontrol.util.Respuesta;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class HostFilter implements Filter {

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse res = (HttpServletResponse) rs;

        boolean estricto = Boolean.parseBoolean(Config.get("host.estricto", "true"));
        String permitidos = Config.get("app.host", "");
        if (!estricto || permitidos.isBlank()) {
            chain.doFilter(rq, rs);
            return;
        }

        String host = req.getServerName();
        boolean ok = host != null && Arrays.stream(permitidos.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(permitido -> coincideHost(host.toLowerCase(), permitido.toLowerCase()));

        if (!ok) {
            Respuesta.error(res, 421, "Host no permitido. La API solo responde en: " + permitidos);
            return;
        }
        chain.doFilter(rq, rs);
    }

    private boolean coincideHost(String host, String permitido) {
        if (permitido.startsWith("*.")) {
            String dominioBase = permitido.substring(2); // ej. "onrender.com"
            return host.equals(dominioBase) || host.endsWith("." + dominioBase);
        }
        return host.equals(permitido);
    }
}
