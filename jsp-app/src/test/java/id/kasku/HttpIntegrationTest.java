package id.kasku;

import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.*;

/** Optional real HTTP/JSP suite: mvn -Dkasku.http=true test (Python 3 required). */
@EnabledIfSystemProperty(named="kasku.http",matches="true")
class HttpIntegrationTest {
    @Test void exerciseAllScreensAndCrudOverHttp() throws Exception {
        Tomcat server=new Tomcat();
        Path temp=Files.createTempDirectory("kasku-http-");server.setBaseDir(temp.toString());server.setPort(0);
        server.getConnector().setProperty("address","127.0.0.1");
        Path source=Path.of("src/main/webapp"),webroot=temp.resolve("webapp");
        try(var files=Files.walk(source)) {
            for(Path file:files.toList()) {
                Path dest=webroot.resolve(source.relativize(file));
                if(Files.isDirectory(file))Files.createDirectories(dest);else Files.copy(file,dest);
            }
        }
        Path config=webroot.resolve("WEB-INF/web.xml");
        Files.writeString(config,Files.readString(config).replace("id.kasku.Bootstrap","id.kasku.HttpTestBootstrap"));
        var context=server.addWebapp("",webroot.toAbsolutePath().toString());context.setParentClassLoader(getClass().getClassLoader());
        try {
            server.start();
            String python=System.getenv().getOrDefault("TEST_PYTHON","python3");
            Process process=new ProcessBuilder(python,"scripts/smoke_test.py","http://127.0.0.1:"+server.getConnector().getLocalPort()).redirectError(ProcessBuilder.Redirect.INHERIT).redirectOutput(Path.of("target/http-test-results.json").toFile()).start();
            assertEquals(0,process.waitFor(),"See target/http-test-results.json and server error output");
        } finally {server.stop();server.destroy();}
    }
}
