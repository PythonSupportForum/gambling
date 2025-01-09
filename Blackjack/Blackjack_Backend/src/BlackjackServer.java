import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class BlackjackServer extends WebSocketServer {

    int count = 0;

    private final Map<WebSocket, GameThread> clientThreads = Collections.synchronizedMap(new HashMap<>());


    public BlackjackServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Neue Verbindung: " + conn.getRemoteSocketAddress());
        conn.send("acc"); // Sende Bestätigung
        GameThread gameThread = new GameThread(count, conn);
        Thread thread = new Thread(gameThread);
        thread.start();
        clientThreads.put(conn, gameThread);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Verbindung geschlossen: " + conn.getRemoteSocketAddress() + ", Grund: " + reason);

        GameThread gameThread = clientThreads.remove(conn);

        if (gameThread != null) {
            gameThread.currentThread.interrupt();
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Nachricht vom Client: " + message);

        // Finde den entsprechenden Thread
        GameThread thread = clientThreads.get(conn);

        if (thread instanceof GameThread) {
            thread.handleMessage(message); // Nachricht an den GameThread weiterleiten
        } else {
            conn.send("Es ist ein Fehler aufgetreten: Kein Thread für diesen Client gefunden.");
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