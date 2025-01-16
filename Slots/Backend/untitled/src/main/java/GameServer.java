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
import java.util.ArrayList;


public class GameServer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DB_URL = "jdbc:mysql://db.ontubs.de:3306/gambling";
    private static final String DB_USER = "larsi";
    private static final String DB_PASSWORD = "geilo123!";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        System.out.println("Server läuft auf http://localhost:8080");

        server.createContext("/bilder", new BilderHandler());
        server.createContext("/start-game", new StartGameHandler());
        server.createContext("/stand", new StandHandler());
        server.createContext("/", new NotFoundHandler());

        server.setExecutor(null);
        server.start();
    }
    public static void addCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    static class StartGameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            GameServer.addCORSHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream requestBody = exchange.getRequestBody();
                HashMap<String, String> requestData = objectMapper.readValue(requestBody, HashMap.class);

                String token = requestData.get("token");

                if (token == null || token.isEmpty() || !isValidToken(token)) {
                    sendJsonResponse(exchange, 401, "Ungültiger Token.");
                    return;
                }

                // Abziehen des Spieleinsatzes
                int betAmount = 1000;
                reduceBalance(token, betAmount);
                recordTransaction(token, -betAmount, "Slots-Einsatz");

                // Starten des Spiels
                int[] results = startGame();
                int winAmount = calculateWin(token, results);

                if (winAmount > 0) {
                    System.out.println("SLot Gewinn: "+winAmount);
                    addBalance(token, winAmount);
                    recordTransaction(token, winAmount, "Slots-Gewinn");
                }

                // Antwort vorbereiten
                HashMap<String, Object> response = new HashMap<>();
                response.put("message", winAmount > 0 ? "Gewonnen!" : "Verloren!");
                response.put("results", results);
                response.put("winAmount", winAmount);

                sendJsonResponse(exchange, 200, response);
            } else {
                System.out.println("Falsche Methode: " + exchange.getRequestMethod());
                sendJsonResponse(exchange, 405, "Method Not Allowed. Bitte verwenden Sie POST.");
            }
        }

        private boolean isValidToken(String token) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT COUNT(*) FROM Kunden WHERE token = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, token);
                    try (ResultSet rs = stmt.executeQuery()) {
                        return rs.next() && rs.getInt(1) > 0;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private int[] startGame() {
            int[] ids = getRoleIds();
            Random random = new Random();
            return new int[]{
                    ids[random.nextInt(ids.length)],
                    ids[random.nextInt(ids.length)],
                    ids[random.nextInt(ids.length)]
            };
        }

        private int[] getRoleIds() {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT id FROM Rollen";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    ArrayList<Integer> ids = new ArrayList<>();
                    while (rs.next()) {
                        ids.add(rs.getInt("id"));
                    }
                    return ids.stream().mapToInt(i -> i).toArray();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new int[0];
        }

        private int calculateWin(String token, int[] results) {
            if (results[0] == results[1] && results[1] == results[2]) {
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String sql = "SELECT gewinn FROM Rollen WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, results[0]);
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) return rs.getInt("gewinn");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }

        private void reduceBalance(String token, int amount) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "UPDATE Kunden SET Kontostand = Kontostand - ? WHERE token = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, amount);
                    stmt.setString(2, token);
                    stmt.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void addBalance(String token, int amount) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "UPDATE Kunden SET Kontostand = Kontostand + ? WHERE token = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, amount);
                    stmt.setString(2, token);
                    stmt.executeUpdate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void recordTransaction(String token, int amount, String type) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "INSERT INTO Transaktionen (Kunden_ID, Betrag, Datum, type) VALUES ((SELECT ID FROM Kunden WHERE token = ?), ?, NOW(), ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, token);
                    stmt.setInt(2, amount);
                    stmt.setString(3, type);
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
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    static class BilderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            GameServer.addCORSHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT id, gewinn, bild FROM Rollen";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        HashMap<String, Object>[] results = processResultSet(rs);

                        sendJsonResponse(exchange, 200, results);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "Interner Serverfehler.");
            }

        }

        private HashMap<String, Object>[] processResultSet(ResultSet rs) throws Exception {
            // Dynamisches Array für Ergebnisse
            ArrayList<HashMap<String, Object>> results = new ArrayList<>();
            while (rs.next()) {
                HashMap<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("gewinn", rs.getInt("gewinn"));
                row.put("bild", rs.getString("bild")); // Bild als Data-URL
                results.add(row);
            }
            return results.toArray(new HashMap[0]);
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
            GameServer.addCORSHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream requestBody = exchange.getRequestBody();
                HashMap<String, String> requestData = objectMapper.readValue(requestBody, HashMap.class);

                String token = requestData.get("token");

                if (token == null || token.isEmpty() || !isValidToken(token)) {
                    sendJsonResponse(exchange, 401, "Ungültiger Token.");
                    return;
                }

                long balance = getBalance(token);

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
                String sql = "SELECT COUNT(*) FROM Kunden WHERE token = ?";
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

        private long getBalance(String token) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String sql = "SELECT Kontostand FROM Kunden WHERE token = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, token);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getLong("Kontostand");
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
