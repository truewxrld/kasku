package id.kasku;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.util.UUID;

/** Test-only listener. Never touches the local or hosted application database. */
public class HttpTestBootstrap implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent event) {
        try {Database.useTestDatabase("jdbc:h2:mem:http"+UUID.randomUUID()+";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");}
        catch(Exception e){throw new IllegalStateException(e);}
    }
}
