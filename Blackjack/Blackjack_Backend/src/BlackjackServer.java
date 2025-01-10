import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Implementation der Websockets gegenüber der unterlegenen Native Sockets (Websockets sind zudem Standardimplementation für javascript clients)
class BlackjackServer extends WebSocketServer {

    // Funktioniert als Client_ID (intern). Mit jedem neuen Client wird diesem eine um 1 höhere ID zugewiesen, da immer um 1 erhöht wird, erhält der erste client die id 0
    int count = -1;

    // "Dictionary" an Threads, Abfrage eines Threads mit Schlüssel Websocket, synchronizedMaps sind thread-sicher
    private final Map<WebSocket, GameThread> clientThreads = Collections.synchronizedMap(new HashMap<>());

    // Konstruktor für die Websocket Implementation
    public BlackjackServer(InetSocketAddress address) {
        super(address);
    }

    // Beim Öffnen einer Verbindung wird ein neuer Thread erstellt, indem nur die eine Verbindung behandelt wird
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        count++;
        System.out.println("Neue Verbindung: " + conn.getRemoteSocketAddress());
        // Senden der ClientId an den Client
        conn.send("acc " + count);

        // Starten eines neuen Threads
        GameThread gameThread = new GameThread(count, conn);
        Thread thread = new Thread(gameThread);
        thread.start();
        // Einfügen in die HashMap ("Dictionary") -> Schlüssel ist die Verbindung
        clientThreads.put(conn, gameThread);
    }

    // Schließen des Threads und entfernen des Threads aus der HashMap
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Verbindung geschlossen: " + conn.getRemoteSocketAddress() + ", Grund: " + reason);

        GameThread gameThread = clientThreads.remove(conn);
        gameThread.handleQuit();
    }

    // Beim Erhalten einer Benachrichtigung → Verteilung der Nachrichten auf die zugehörigen Threads
    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Nachricht vom Client: " + message);
        // Finde den entsprechenden Thread durch die verwendete Verbindung als Schlüssel
        GameThread thread = clientThreads.get(conn);

        if (thread != null) {
            thread.handleMessage(message); // Nachricht an den zugehörigen GameThread weiterleiten
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
        System.out.println("Blackjack Server wurde erfolgreich gestartet.");
    }
}