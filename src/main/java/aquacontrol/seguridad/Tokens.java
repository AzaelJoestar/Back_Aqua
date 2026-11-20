package aquacontrol.seguridad;

import aquacontrol.config.Config;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.time.Instant;
import java.util.logging.Logger;

/** Crea y valida tokens JWT firmados con HMAC-SHA256. */
public final class Tokens {
    private static final Logger LOG = Logger.getLogger(Tokens.class.getName());
    private static final String EMISOR = "aquacontrol";
    private static final Algorithm ALGORITMO = crearAlgoritmo();

    private Tokens() {}

    private static Algorithm crearAlgoritmo() {
        String secreto = Config.get("jwt.secret", "");
        if (secreto.length() < 32 || secreto.startsWith("cambia-esta-clave")) {
            LOG.warning("jwt.secret es débil o es el valor de ejemplo. Cámbialo antes de publicar el sistema.");
        }
        return Algorithm.HMAC256(secreto.isEmpty() ? "sin-configurar" : secreto);
    }

    public static long horasDeVida() {
        return Config.entero("jwt.horas", 8);
    }

    public static String crear(int idUsuario, String usuario, String rol) {
        Instant ahora = Instant.now();
        return JWT.create()
                .withIssuer(EMISOR)
                .withSubject(String.valueOf(idUsuario))
                .withClaim("usuario", usuario)
                .withClaim("rol", rol)
                .withIssuedAt(ahora)
                .withExpiresAt(ahora.plusSeconds(horasDeVida() * 3600))
                .sign(ALGORITMO);
    }

    public static DecodedJWT verificar(String token) throws JWTVerificationException {
        return JWT.require(ALGORITMO).withIssuer(EMISOR).build().verify(token);
    }
}
