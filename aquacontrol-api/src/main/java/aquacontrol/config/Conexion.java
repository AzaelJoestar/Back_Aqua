package aquacontrol.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/** Pool de conexiones (HikariCP). Reutiliza conexiones abiertas en lugar de abrir una por petición. */
public final class Conexion {
    private static HikariDataSource fuente;

    private Conexion() {}

    /** Arma la URL con host, puerto y nombre de la base (db.properties). No hay host por defecto. */
    static String url() {
        String host = Config.get("db.host");
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("Falta db.host en db.properties");
        }
        String sslMode = Config.get("db.sslmode");
        if (sslMode == null || sslMode.isBlank()) {
            sslMode = "require";
        }
        return "jdbc:postgresql://" + host.trim() + ":" + Config.entero("db.port", 5432)
                + "/" + Config.get("db.name", "purificadora")
                + "?sslmode=" + sslMode.trim();
    }

    private static synchronized HikariDataSource fuente() {
        if (fuente == null) {
            HikariConfig cfg = new HikariConfig();
            cfg.setDriverClassName("org.postgresql.Driver");
            cfg.setJdbcUrl(url());
            cfg.setUsername(Config.get("db.user"));
            cfg.setPassword(Config.get("db.password"));
            cfg.setMaximumPoolSize(Config.entero("db.pool.max", 10));
            cfg.setMinimumIdle(2);
            cfg.setConnectionTimeout(10_000);
            cfg.setPoolName("aquacontrol-pool");
            fuente = new HikariDataSource(cfg);
        }
        return fuente;
    }

    /** Toma una conexión del pool. Al cerrarla (try-with-resources) regresa al pool. */
    public static Connection get() throws SQLException {
        return fuente().getConnection();
    }

    public static synchronized void cerrar() {
        if (fuente != null) {
            fuente.close();
            fuente = null;
        }
    }
}
