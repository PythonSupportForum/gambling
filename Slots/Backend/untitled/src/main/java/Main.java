import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Main {
    // JSON Mapper
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Datenbankverbindung konfigurieren
    private static final String DB_URL = "jdbc:mysql://db.ontubs.de:3306/gambling";
    private static final String DB_USER = "larsi";
    private static final String DB_PASSWORD = "geilo123!";

    public static void main(String[] args) throws IOException {
        // HTTP-Server erstellen
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("Server läuft auf http://localhost:8080");

        // Route für Benutzerregistrierung hinzufügen
        server.createContext("/register", new RegisterHandler());

        // Server starten
        server.setExecutor(null); // Standardexecutor verwenden
        server.start();
    }

    // Handler für Benutzerregistrierung
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // JSON-Daten aus der Anfrage lesen
                InputStream requestBody = exchange.getRequestBody();
                User user = objectMapper.readValue(requestBody, User.class);

                // Benutzer in die Datenbank einfügen
                boolean success = registerUser(user);

                // Antwort senden
                String response = success ? "Benutzer erfolgreich registriert!" : "Fehler bei der Registrierung.";
                exchange.sendResponseHeaders(success ? 200 : 500, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                // Methode nicht erlaubt
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }

        // Benutzer in die Datenbank einfügen
        private boolean registerUser(User user) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, user.getUsername());
                    stmt.setString(2, user.getPassword());
                    stmt.setString(3, user.getEmail());
                    stmt.executeUpdate();
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    // Benutzerklasse für JSON-Serialisierung/Deserialisierung
    static class User {
        private String username;
        private String password;
        private String email;

        // Getter und Setter
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
