import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Random;

public class GameServer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DB_URL = "jdbc:mysql://db.ontubs.de:3306/gambling";
    private static final String DB_USER = "larsi";
    private static final String DB_PASSWORD = "geilo123!";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("Server läuft auf http://localhost:8080");

        server.createContext("/start-game", new StartGameHandler());
        server.createContext("/stand", new StandHandler());
        server.createContext("/", new NotFoundHandler());

        server.setExecutor(null);
        server.start();
    }
    private static void addCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    // Handler für das Starten des Spiels
    static class StartGameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCORSHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream requestBody = exchange.getRequestBody();
                HashMap<String, String> requestData = objectMapper.readValue(requestBody, HashMap.class);

                String token = requestData.get("token");

                if (token == null || token.isEmpty() || !isValidToken(token)) {
                    sendJsonResponse(exchange, 401, "Ungültiger Token.");
                    return;
                }

                int[] results = startGame();
                reduceBalance(token, 1000);

                HashMap<String, Object> response = new HashMap<>();
                response.put("message", "Spiel erfolgreich gestartet!");
                response.put("results", results);

                sendJsonResponse(exchange, 200, response);
            } else {
                // Methode nicht erlaubt
                sendJsonResponse(exchange, 405, "Method Not Allowed. Bitte verwenden Sie POST.");
            }
        }


        private boolean isValidToken(String token) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT COUNT(*) FROM users WHERE token = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, token);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private int[] startGame() {
            Random random = new Random();
            return new int[]{random.nextInt(100), random.nextInt(100), random.nextInt(100)};
        }

        private void reduceBalance(String token, int amount) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "UPDATE users SET balance = balance - ? WHERE token = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, amount);
                    stmt.setString(2, token);
                    stmt.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
            String response = objectMapper.writeValueAsString(data);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class StandHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCORSHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream requestBody = exchange.getRequestBody();
                HashMap<String, String> requestData = objectMapper.readValue(requestBody, HashMap.class);

                String token = requestData.get("token");

                if (token == null || token.isEmpty() || !isValidToken(token)) {
                    sendJsonResponse(exchange, 401, "Ungültiger Token.");
                    return;
                }

                int balance = getBalance(token);

                HashMap<String, Object> response = new HashMap<>();
                response.put("message", "Kontostand erfolgreich abgefragt.");
                response.put("balance", balance);

                sendJsonResponse(exchange, 200, response);
            } else {
                sendJsonResponse(exchange, 405, "Method Not Allowed. Bitte verwenden Sie POST.");
            }
        }

        private boolean isValidToken(String token) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT COUNT(*) FROM users WHERE token = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, token);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private int getBalance(String token) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT balance FROM users WHERE token = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, token);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt("balance");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
            String response = objectMapper.writeValueAsString(data);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    // Handler für ungültige Pfade
    static class NotFoundHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendJsonResponse(exchange, 404, "Pfad nicht gefunden.");
        }

        private void sendJsonResponse(HttpExchange exchange, int statusCode, Object data) throws IOException {
            String response = objectMapper.writeValueAsString(data);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
