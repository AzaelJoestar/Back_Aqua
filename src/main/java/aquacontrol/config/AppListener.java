package aquacontrol.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.List;
import java.util.logging.Logger;

/** Al arrancar avisa si la configuración usa localhost; al detener cierra el pool de conexiones. */
@WebListener
public class AppListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(AppListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        for (String clave : List.of("app.host", "db.host", "cors.origen")) {
            String valor = Config.get(clave, "");
            if (Config.esLocalhost(valor))
                LOG.warning(clave + " usa localhost (" + valor + "). Configura un nombre de host específico (ver README).");
        }
        if (Config.get("app.host", "").isBlank())
            LOG.warning("app.host no está configurado: la API aceptará cualquier host.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Conexion.cerrar();
    }
}
