package id.kasku;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class Bootstrap implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent event) {
        try {
            Database.init();
            event.getServletContext().getSessionCookieConfig().setSecure(Boolean.parseBoolean(System.getenv().getOrDefault("COOKIE_SECURE", "false")));
        } catch (Exception e) { throw new IllegalStateException("Database initialization failed", e); }
    }
}
