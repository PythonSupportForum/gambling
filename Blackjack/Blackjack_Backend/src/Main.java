import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.io.IOException;

public class Main {

    public static void main(String[] args){

        int port = 8080;

        WebSocketServer server = new BlackjackServer(new InetSocketAddress(port));
        server.start();
        System.out.println("WebSocket-Server gestartet auf Port: " + port);
    }


}