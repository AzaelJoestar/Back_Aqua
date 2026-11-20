package aquacontrol.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/** Pool de conexiones (HikariCP). Reutiliza conexiones abiertas en lugar de abrir una por petición. */
public final class Conexion {
    private static HikariDataSource fuente;

    private Conexion() {}

    private static synchronized HikariDataSource fuente() {
        if (fuente == null) {
            HikariConfig cfg = new HikariConfig();
            cfg.setDriverClassName("org.postgresql.Driver");
            cfg.setJdbcUrl(Config.get("db.url"));
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
