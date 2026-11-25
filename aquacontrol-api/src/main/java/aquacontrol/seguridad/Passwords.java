package aquacontrol.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.mindrot.jbcrypt.BCrypt;

/** Las contraseñas se guardan con BCrypt (hash con sal); nunca se almacena ni se devuelve la original. */
public final class Passwords {
    private Passwords() {}

    public static String cifrar(String plana) {
        return BCrypt.hashpw(plana, BCrypt.gensalt(10));
    }

    public static boolean esHash(String almacenada) {
        return almacenada != null && almacenada.startsWith("$2");
    }

    /**
     * Compara contra el hash. Si en la BD quedó alguna contraseña vieja en texto plano
     * la compara tal cual; AuthServlet la convierte a hash en el primer login correcto.
     */
    public static boolean verificar(String plana, String almacenada) {
        if (plana == null || almacenada == null) return false;
        if (esHash(almacenada)) {
            try {
                return BCrypt.checkpw(plana, almacenada);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return MessageDigest.isEqual(plana.getBytes(StandardCharsets.UTF_8), almacenada.getBytes(StandardCharsets.UTF_8));
    }
}
