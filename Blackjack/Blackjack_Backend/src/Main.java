import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args){

        int port = 8080;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server gestartet, wartet auf Verbindungen...");
            while (true)
            {
                // Warten auf eine Verbindung vom Client
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client verbunden: " + clientSocket.getInetAddress());

                // Streams zum Senden und Empfangen
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

                // Nachricht vom Client lesen
                String clientMessage;
                while ((clientMessage = input.readLine()) != null) {
                    // Erwartet client_ID in der ersten Nachricht des Clients
                    int client_ID = Integer.parseInt(clientMessage);
                    // Bestätigung an den Client
                    output.println("acc");
                    startSession(client_ID, clientSocket);

                    // Beenden, falls "exit" gesendet wird
                    if (clientMessage.equalsIgnoreCase("exit")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startSession(int ID, Socket clientSocket){
        Thread thread = new Thread(new GameThread(ID));
        thread.start();
    }
}