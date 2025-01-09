import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

class BlackjackServer extends WebSocketServer {

    public BlackjackServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(org.java_websocket.WebSocket conn, ClientHandshake handshake) {
        System.out.println("Neue Verbindung: " + conn.getRemoteSocketAddress());
        conn.send("acc"); // Sende Willkommensnachricht
    }

    @Override
    public void onClose(org.java_websocket.WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Verbindung geschlossen: " + conn.getRemoteSocketAddress() + ", Grund: " + reason);
    }

    @Override
    public void onMessage(org.java_websocket.WebSocket conn, String message) {
        System.out.println("Nachricht vom Client: " + message);

        // Beispiel: Behandeln bestimmter Befehle
        if (message.equalsIgnoreCase("start")) {
            conn.send("Spiel startet jetzt!");
            GameThread game = new GameThread(conn.hashCode());
            new Thread(game).start();
        } else if (message.equalsIgnoreCase("exit")) {
            conn.send("Spiel wird beendet. Verabschiedung!");
            conn.close();
        } else {
            conn.send("Unbekannter Befehl: " + message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("Ein Fehler ist aufgetreten: " + ex.getMessage());
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket-Server wurde erfolgreich gestartet.");
    }
}