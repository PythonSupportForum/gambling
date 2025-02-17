import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// Implementation der Websockets gegenüber der unterlegenen Native Sockets (Websockets sind zudem Standardimplementation für javascript Clients)
class BlackjackServer extends WebSocketServer {
    private ClientHandshake handshake;

    boolean acc = false; //Ob schon die Token Autentifizierung war, eig. Unnötig, da er erst nach Token weiter

    // "Dictionary" an Threads, Abfrage eines Threads mit Schlüssel Websocket, synchronizedMaps sind thread-sicher
    private final Map<WebSocket, GameThread> clientThreads = Collections.synchronizedMap(new HashMap<>());
    // Construktor für die Websocket Implementation
    public BlackjackServer(InetSocketAddress address) {
        super(address);
    }
    // Beim Öffnen einer Verbindung wird ein neuer Thread erstellt, indem nur die eine Verbindung behandelt wird
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("Neue Verbindung verbunden: " + conn.getRemoteSocketAddress());

        //Damit die Verbindung von üpberal aufgebraut werden kann und Browser nicht blokieren wegen verscheidneen Origins aus Sicherheitsgründen Sicherheitsmethoden abschalten
        conn.setAttachment("Access-Control-Allow-Origin: *");
        conn.setAttachment("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
        conn.setAttachment("Access-Control-Allow-Headers: Content-Type, Authorization");

        conn.send("acc");
        System.out.println("acc " + conn.getRemoteSocketAddress());
    }

    // Schließen des Threads und entfernen des Threads aus der HashMap
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Verbindung wurde geschlossen: " + conn.getRemoteSocketAddress() + ", Grund: " + reason);

        GameThread gameThread = clientThreads.remove(conn);
        if(gameThread != null) gameThread.handleQuit();
    }

    // Beim Erhalten einer Benachrichtigung → Verteilung der Nachrichten auf die zugehörigen Threads (Broadcasting)
    @Override
    public void onMessage(WebSocket conn, String message) {
        String lowerCaseMessage = message.toLowerCase();

        // Finde den entsprechenden Thread durch die verwendete Verbindung als Schlüssel fuer das Dictionary
        GameThread thread = clientThreads.get(conn);

        if (thread != null) thread.handleMessage(lowerCaseMessage); // Nachricht an den zugehörigen GameThread weiterleiten
        else {
            System.out.println("Message without thread: " + lowerCaseMessage);
            int startIndex = lowerCaseMessage.indexOf("id:");
            if (startIndex != -1) {
                try {
                    String token = message.substring(startIndex + "id:".length()); //Token enthält auch Großbuchstaben
                    System.out.println("Token: " + token);
                    acc = true;
                    newThread(conn, token); // erstellt einen GameThread, wenn ein Token uebermittelt wird
                } catch(NumberFormatException e){
                    System.out.println("Fehler: ID ist keine Zahl");
                }
            } else{
                System.out.println("Fehler: Befehl nicht erkannt");
            }
        }
    }
    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.out.println("Ein Fehler ist aufgetreten: " + ex.getMessage());
        clientThreads.remove(conn);
        ex.printStackTrace();
    }
    @Override
    public void onStart() {
        System.out.println("Blackjack Server wurde erfolgreich gestartet.");
    }

    void newThread(WebSocket conn, String token) {
        // Starten eines neuen Threads
        GameThread gameThread = new GameThread(token, conn);
        Thread thread = new Thread(gameThread);
        thread.start();
        // Einfügen in die HashMap ("Dictionary") -> Schlüssel ist der Websocket
        clientThreads.put(conn, gameThread);
    }
}