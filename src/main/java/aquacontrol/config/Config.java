package aquacontrol.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Lee db.properties y app.properties. Una variable de entorno AQUACONTROL_* tiene prioridad. */
public final class Config {
    private static final Properties PROPS = new Properties();

    static {
        cargar("db.properties");
        cargar("app.properties");
    }

    private Config() {}

    private static void cargar(String nombre) {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(nombre)) {
            if (in != null) PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + nombre, e);
        }
    }

    public static String get(String clave, String defecto) {
        String env = System.getenv("AQUACONTROL_" + clave.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) return env;
        return PROPS.getProperty(clave, defecto);
    }

    public static String get(String clave) {
        return get(clave, null);
    }

    public static int entero(String clave, int defecto) {
        String v = get(clave);
        return v == null ? defecto : Integer.parseInt(v.trim());
    }

    /** true si el valor apunta a la propia máquina (localhost, 127.x o ::1). */
    public static boolean esLocalhost(String valor) {
        if (valor == null) return false;
        String v = valor.toLowerCase();
        return v.contains("localhost") || v.contains("127.0.0.1") || v.contains("[::1]");
    }
}
