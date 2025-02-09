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
    int id = -50;
    boolean acc = false;

    // "Dictionary" an Threads, Abfrage eines Threads mit Schlüssel Websocket, synchronizedMaps sind thread-sicher
    private final Map<WebSocket, GameThread> clientThreads = Collections.synchronizedMap(new HashMap<>());

    // Konstruktor für die Websocket Implementation
    public BlackjackServer(InetSocketAddress address) {
        super(address);
    }

    // Beim Öffnen einer Verbindung wird ein neuer Thread erstellt, indem nur die eine Verbindung behandelt wird
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Neue Verbindung: " + conn.getRemoteSocketAddress());
        conn.send("acc");
        System.out.println("acc " + conn.getRemoteSocketAddress());
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
        String lowerCaseMessage = message.toLowerCase();

        // Finde den entsprechenden Thread durch die verwendete Verbindung als Schlüssel
        GameThread thread = clientThreads.get(conn);

        if (thread != null) {
            thread.handleMessage(lowerCaseMessage); // Nachricht an den zugehörigen GameThread weiterleiten
        } else {
            System.out.println("Message without thread: " + lowerCaseMessage);
            int startIndex = lowerCaseMessage.indexOf("id:");
            if (startIndex != -1) {
                try{
                    id = Integer.parseInt(lowerCaseMessage.substring(startIndex + "id:".length()).trim());
                    System.out.println("ID: " + id);
                    acc = true;
                    newThread(conn);
                }catch(NumberFormatException e){
                    System.out.println("Fehler: ID ist keine Zahl");
                }
            }
            else{
                System.out.println("Fehler: Befehl nicht erkannt");
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("Ein Fehler ist aufgetreten: " + ex.getMessage());
        ex.printStackTrace();
        conn.close();
        clientThreads.remove(conn);
        try {
            this.stop();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStart() {
        System.out.println("Blackjack Server wurde erfolgreich gestartet.");
    }

    void newThread(WebSocket conn) {
        // Starten eines neuen Threads
        GameThread gameThread = new GameThread(id, conn);
        Thread thread = new Thread(gameThread);
        thread.start();
        // Einfügen in die HashMap ("Dictionary") -> Schlüssel ist der Websocket
        clientThreads.put(conn, gameThread);
    }
}