package aquacontrol.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Cierra el pool cuando Tomcat detiene o recarga la aplicación. */
@WebListener
public class AppListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Conexion.cerrar();
    }
}
