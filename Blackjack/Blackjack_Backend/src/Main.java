import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class Main {

    // Main Methode
    public static void main(String[] args){

        // Port zum Hosten des Servers
        int port = 8080;

        // Ein Server wird mit der Addresse des ausführenden Servers erstellt
        WebSocketServer server = new BlackjackServer(new InetSocketAddress(port));
        server.start();
    }
}