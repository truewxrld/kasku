package id.kasku;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of(".runtime"));
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(Path.of(".runtime").toAbsolutePath().toString());
        int port=Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        for(int i=0;i<args.length-1;i++) if(args[i].equals("--port")) port=Integer.parseInt(args[i+1]);
        tomcat.setPort(port);
        Connector connector = tomcat.getConnector();
        connector.setProperty("address", "0.0.0.0");
        connector.setProperty("maxThreads", "40");
        connector.setProperty("maxHttpRequestHeaderSize", "16384");
        Context context = tomcat.addWebapp("", Path.of("src/main/webapp").toAbsolutePath().toString());
        context.setParentClassLoader(Main.class.getClassLoader());
        Rfc6265CookieProcessor cookies = new Rfc6265CookieProcessor();
        cookies.setSameSiteCookies("lax");
        context.setCookieProcessor(cookies);
        tomcat.start();
        System.out.println("KasKu ready on port " + connector.getPort());
        tomcat.getServer().await();
    }
}
