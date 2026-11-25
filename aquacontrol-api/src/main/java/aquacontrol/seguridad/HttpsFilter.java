package aquacontrol.seguridad;

import aquacontrol.config.Config;
import aquacontrol.util.Respuesta;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Cifrado en tránsito: las rutas de venta y pago (ver web.xml) solo se aceptan por HTTPS (TLS).
 * Por HTTP plano se rechaza con 403, para que usuario, contraseña, ventas y pagos nunca viajen en claro.
 * Si la respuesta va por HTTPS agrega HSTS para que el navegador no vuelva a usar HTTP.
 * Se puede apagar con https.requerido=false (solo para pruebas).
 */
public class HttpsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse res = (HttpServletResponse) rs;

        if (!Boolean.parseBoolean(Config.get("https.requerido", "true")) || "OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(rq, rs);
            return;
        }
        if (!esSeguro(req)) {
            Respuesta.error(res, 403, "Esta operación solo se permite por HTTPS");
            return;
        }
        res.setHeader("Strict-Transport-Security", "max-age=31536000");
        chain.doFilter(rq, rs);
    }

    /** Conexión TLS directa, o detrás de un proxy de confianza que avisa con X-Forwarded-Proto. */
    static boolean esSeguro(HttpServletRequest req) {
        if (req.isSecure()) return true;
        return Boolean.parseBoolean(Config.get("https.proxy.confiable", "false"))
                && "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
    }
}
