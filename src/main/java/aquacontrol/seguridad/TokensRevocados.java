package aquacontrol.seguridad;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tokens JWT cerrados con "cerrar sesión" (DELETE /api/auth/sesion).
 * Un JWT no se puede borrar, así que se recuerda su identificador (jti) hasta que venza.
 * Se guarda en memoria: si Tomcat se reinicia, la lista se vacía (los tokens viejos vencen solos).
 */
public final class TokensRevocados {
    private static final Map<String, Instant> REVOCADOS = new ConcurrentHashMap<>();

    private TokensRevocados() {}

    public static void revocar(String jti, Instant venceEn) {
        if (jti == null) return;
        Instant ahora = Instant.now();
        REVOCADOS.entrySet().removeIf(e -> e.getValue().isBefore(ahora)); // limpia los ya vencidos
        REVOCADOS.put(jti, venceEn == null ? ahora.plusSeconds(Tokens.horasDeVida() * 3600) : venceEn);
    }

    public static boolean estaRevocado(String jti) {
        return jti != null && REVOCADOS.containsKey(jti);
    }
}
