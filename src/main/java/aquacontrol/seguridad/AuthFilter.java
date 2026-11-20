package aquacontrol.seguridad;

import aquacontrol.config.Config;
import aquacontrol.util.Respuesta;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Exige un token JWT válido (Authorization: Bearer ...) en las rutas con información delicada
 * (ver web.xml). Usuarios y roles, además, solo los puede usar el rol administrador.
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse res = (HttpServletResponse) rs;

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) { // preflight de CORS, sin credenciales
            chain.doFilter(rq, rs);
            return;
        }

        String cabecera = req.getHeader("Authorization");
        if (cabecera == null || !cabecera.regionMatches(true, 0, "Bearer ", 0, 7)) {
            Respuesta.error(res, 401, "Se requiere iniciar sesión");
            return;
        }

        DecodedJWT jwt;
        try {
            jwt = Tokens.verificar(cabecera.substring(7).trim());
        } catch (JWTVerificationException e) {
            Respuesta.error(res, 401, "Sesión inválida o expirada");
            return;
        }

        String rol = jwt.getClaim("rol").asString();
        String ruta = req.getRequestURI().substring(req.getContextPath().length());
        boolean soloAdmin = ruta.startsWith("/api/usuarios") || ruta.startsWith("/api/roles-permisos");
        if (soloAdmin && (rol == null || !rol.equalsIgnoreCase(Config.get("rol.admin", "admin")))) {
            Respuesta.error(res, 403, "No tienes permiso para esta sección");
            return;
        }

        req.setAttribute("idUsuario", Integer.valueOf(jwt.getSubject()));
        req.setAttribute("rol", rol);
        chain.doFilter(rq, rs);
    }
}
