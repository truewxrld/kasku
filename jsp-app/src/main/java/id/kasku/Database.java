package id.kasku;

import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

public final class Database {
    private static String url, user, password;
    private Database() {}

    public static void init() throws Exception {
        String raw = System.getenv("DATABASE_URL");
        if (raw != null && !raw.isBlank()) {
            URI uri = URI.create(raw);
            if (!Set.of("postgres", "postgresql").contains(uri.getScheme())) throw new IllegalArgumentException("Use a PostgreSQL DATABASE_URL");
            String[] credentials = uri.getRawUserInfo().split(":", 2);
            user = URLDecoder.decode(credentials[0].replace("+", "%2B"), StandardCharsets.UTF_8);
            password = credentials.length == 2 ? URLDecoder.decode(credentials[1].replace("+", "%2B"), StandardCharsets.UTF_8) : "";
            url = "jdbc:postgresql://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "") + uri.getPath();
            url += "?" + (uri.getRawQuery() == null ? "sslmode=require" : uri.getRawQuery());
        } else if (System.getenv("JDBC_DATABASE_URL") != null) {
            url = System.getenv("JDBC_DATABASE_URL");
            user = System.getenv().getOrDefault("DB_USER", "sa");
            password = System.getenv().getOrDefault("DB_PASSWORD", "");
        } else {
            if ("production".equals(System.getenv("APP_ENV"))) throw new IllegalStateException("Set DATABASE_URL for production; file database is local only.");
            Files.createDirectories(Path.of("data"));
            url = "jdbc:h2:file:./data/kasku;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_ON_EXIT=FALSE";
            user = "sa"; password = "";
        }
        initializeSchema();
    }
    static void useTestDatabase(String jdbc) throws Exception {
        url = jdbc; user = "sa"; password = ""; initializeSchema();
    }
    private static void initializeSchema() throws Exception {
        Class.forName(url.startsWith("jdbc:h2:") ? "org.h2.Driver" : "org.postgresql.Driver");
        try (Connection c = connection(); Statement s = c.createStatement(); var input = Database.class.getResourceAsStream("/schema.sql")) {
            if (input == null) throw new IllegalStateException("schema.sql missing");
            String schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : schema.split(";")) if (!sql.isBlank()) s.execute(sql);
        }
    }
    public static Connection connection() throws SQLException { return DriverManager.getConnection(url, user, password); }
    public static List<Map<String,Object>> query(String sql, Object... values) throws SQLException {
        try (Connection c = connection()) { return query(c, sql, values); }
    }
    public static List<Map<String,Object>> query(Connection c, String sql, Object... values) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(sql)) {
            bind(s, values);
            try (ResultSet rs = s.executeQuery()) {
                List<Map<String,Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<>();
                    for (int i=1;i<=rs.getMetaData().getColumnCount();i++) row.put(rs.getMetaData().getColumnLabel(i).toLowerCase(Locale.ROOT), rs.getObject(i));
                    rows.add(row);
                }
                return rows;
            }
        }
    }
    public static int update(String sql, Object... values) throws SQLException {
        try (Connection c=connection(); PreparedStatement s=c.prepareStatement(sql)) { bind(s,values); return s.executeUpdate(); }
    }
    public static long insert(Connection c, String sql, Object... values) throws SQLException {
        try (PreparedStatement s=c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(s,values); s.executeUpdate();
            try (ResultSet rs=s.getGeneratedKeys()) { rs.next(); return rs.getLong(1); }
        }
    }
    public static long insert(String sql, Object... values) throws SQLException { try (Connection c=connection()) {return insert(c, sql,values);} }
    public static void bind(PreparedStatement s, Object... values) throws SQLException { for (int i=0;i<values.length;i++) s.setObject(i+1,values[i]); }
}
