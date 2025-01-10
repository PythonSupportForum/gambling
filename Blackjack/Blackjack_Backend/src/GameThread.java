import org.java_websocket.WebSocket;

import java.util.*;

// Implementiert das Runnable interface -> Nutzung der Java Implementation für Multithreading
public class GameThread implements Runnable {

    WebSocket conn;
    int client_ID;
    // Ermöglicht Zugriff auf Thread Objekt, wenn GameThread Objekt gefunden wurde
    public Thread currentThread = Thread.currentThread();

    public enum GameState {
        IDLE,
        DEPOSIT,
        START,
        BET,
        SHUFFLE,
        DEALER_START,
        PLAYER_DRAW,
        DEALER_DRAW,
        PLAYER_WON,
        PLAYER_LOST,
        WITHDRAW
    }

    // Beschreibt den Status, indem sich das Spiel befindet
    GameState gameState = GameState.IDLE;

    // Konstruktor
    public GameThread(int id, WebSocket _conn) {
        client_ID = id;
        conn = _conn;
    }

    // Implementation der run() - Methode des Runnable Interfaces, erste Funktion die nach der Öffnung des Threads ausgeführt wird
    public void run() {
        game(client_ID); // ruft Hauptmethode des Spiels auf, beginnt Spiel mit dem Client
    }

    // Verarbeiten einer einkommenden Nachricht vom Client
    public void handleMessage(String message) {
        switch (message.toLowerCase()) {
            case "start":
                conn.send("Spiel wurde gestartet!");
                break;
            case "exit":
                conn.send("Spiel wird beendet.");
                conn.close(); // Verbindung beenden
                currentThread.interrupt();// Beende den Thread
                break;
            default:
                conn.send("Unbekannter Befehl: " + message);
                break;
        }
    }


    // Funktioniert als Hauptmethode für das Blackjack Spiel
    public void game(int client_ID) {
        //Start des Spiels
        int coins = 0;
        double balance = 0.0;
        int bet = 0;
        // Kontostand abfragen in der Datenbank

        List<GameCard> availableCards = new ArrayList<GameCard>();
        List<GameCard> temp = new ArrayList<GameCard>();
        //region Karten hinzufügen
        // Clubs (Kreuz)
        availableCards.add(new GameCard('2', 'c'));
        availableCards.add(new GameCard('3', 'c'));
        availableCards.add(new GameCard('4', 'c'));
        availableCards.add(new GameCard('5', 'c'));
        availableCards.add(new GameCard('6', 'c'));
        availableCards.add(new GameCard('7', 'c'));
        availableCards.add(new GameCard('8', 'c'));
        availableCards.add(new GameCard('9', 'c'));
        availableCards.add(new GameCard('0', 'c')); // 10
        availableCards.add(new GameCard('j', 'c')); // Bube
        availableCards.add(new GameCard('q', 'c')); // Dame
        availableCards.add(new GameCard('k', 'c')); // König
        availableCards.add(new GameCard('a', 'c')); // Ass

        // Diamonds (Karo)
        availableCards.add(new GameCard('2', 'd'));
        availableCards.add(new GameCard('3', 'd'));
        availableCards.add(new GameCard('4', 'd'));
        availableCards.add(new GameCard('5', 'd'));
        availableCards.add(new GameCard('6', 'd'));
        availableCards.add(new GameCard('7', 'd'));
        availableCards.add(new GameCard('8', 'd'));
        availableCards.add(new GameCard('9', 'd'));
        availableCards.add(new GameCard('0', 'd')); // 10
        availableCards.add(new GameCard('j', 'd')); // Bube
        availableCards.add(new GameCard('q', 'd')); // Dame
        availableCards.add(new GameCard('k', 'd')); // König
        availableCards.add(new GameCard('a', 'd')); // Ass

        // Hearts (Herz)
        availableCards.add(new GameCard('2', 'h'));
        availableCards.add(new GameCard('3', 'h'));
        availableCards.add(new GameCard('4', 'h'));
        availableCards.add(new GameCard('5', 'h'));
        availableCards.add(new GameCard('6', 'h'));
        availableCards.add(new GameCard('7', 'h'));
        availableCards.add(new GameCard('8', 'h'));
        availableCards.add(new GameCard('9', 'h'));
        availableCards.add(new GameCard('0', 'h')); // 10
        availableCards.add(new GameCard('j', 'h')); // Bube
        availableCards.add(new GameCard('q', 'h')); // Dame
        availableCards.add(new GameCard('k', 'h')); // König
        availableCards.add(new GameCard('a', 'h')); // Ass

        // Spades (Pik)
        availableCards.add(new GameCard('2', 's'));
        availableCards.add(new GameCard('3', 's'));
        availableCards.add(new GameCard('4', 's'));
        availableCards.add(new GameCard('5', 's'));
        availableCards.add(new GameCard('6', 's'));
        availableCards.add(new GameCard('7', 's'));
        availableCards.add(new GameCard('8', 's'));
        availableCards.add(new GameCard('9', 's'));
        availableCards.add(new GameCard('0', 's')); // 10
        availableCards.add(new GameCard('j', 's')); // Bube
        availableCards.add(new GameCard('q', 's')); // Dame
        availableCards.add(new GameCard('k', 's')); // König
        availableCards.add(new GameCard('a', 's')); // Ass
        // endregion

        List<GameCard> dealerStack = new ArrayList<>();
        List<GameCard> playerStack = new ArrayList<>();
        Stack<GameCard> deck = new Stack<>();
        setGameState(GameState.DEPOSIT);

        boolean input = false;
        int coinAmount = 0;

        while (!input) {
            //ist nich vollständig, nach mit Frontend lösen
            Scanner c = new Scanner(System.in);
            String inputString = c.nextLine();
            try
            {
                coinAmount = Integer.parseInt(inputString);
                input = true;
            }
            catch(NumberFormatException e)
            {
                continue;
            }

        }

         //Der Input vom Spieler, temporäre Variable
        if(balance - (coinAmount * 100) < 0){
            //Fehler an Spieler zurücksenden, zu wenig Geld
        } else {
            coins += coinAmount;
            balance -= coinAmount * 100;
        }

        setGameState(GameState.START);
        setGameState(GameState.BET);
        boolean input2 = false;
        while (!input2) {
            //ist nich vollständig, nach mit Frontend lösen
            Scanner c = new Scanner(System.in);

        }
        setGameState(GameState.SHUFFLE);


        while (!availableCards.isEmpty()) {
            Random b = new Random();
            int random = b.nextInt(availableCards.size());
            deck.push(availableCards.get(random));
            availableCards.remove(random);
        }

        setGameState(GameState.DEALER_START);

        dealerStack.add(deck.pop());
        dealerStack.add(deck.pop());
        if (dealerStack.get(1).getCoat() == 'a') {
            // ask for Insurance Bet
        }

        setGameState(GameState.PLAYER_DRAW);
        boolean input3 = false;
        while (!input3) {
            //ist nich vollständig, nach mit Frontend lösen
            Scanner c = new Scanner(System.in);
        }

        setGameState(GameState.DEALER_DRAW);

        //Hier gehört die Dealer Algorithmus Funktion hin

        setGameState(GameState.PLAYER_WON);
        setGameState(GameState.PLAYER_LOST);
        setGameState(GameState.WITHDRAW);
    }

    //region Getter and Setter
    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    //endregion


    //region Dealer Algorithmus, später mit Frontend troubleshooten
    //dealerAlgorithm(dealerStack, deck);

    private void dealerAlgorithm(List<GameCard> dealerStack, Stack<GameCard> deck) {
        int total = 0;
        boolean hasAce = false;

        // Calculate initial hand value
        for (GameCard card : dealerStack) {
            if (card.getCoat() == 'a') {
                hasAce = true;
                total += 11;
            } else if (card.getCoat() == 'j' || card.getCoat() == 'q' || card.getCoat() == 'k' || card.getCoat() == '0') {
                total += 10;
            } else {
                total += Character.getNumericValue(card.getCoat());
            }
        }

        // Adjust Ace value if necessary
        if (hasAce && total > 21) {
            total -= 10; // Count Ace as 1 instead of 11
        }

        // Draw until the dealer's hand reaches a stable strategy threshold
        while (total < 17 || (total < 18 && hasAce)) {
            GameCard newCard = deck.pop();
            dealerStack.add(newCard);

            if (newCard.getCoat() == 'a') {
                hasAce = true;
                total += 11;
            } else if (newCard.getCoat() == 'j' || newCard.getCoat() == 'q' || newCard.getCoat() == 'k' || newCard.getCoat() == '0') {
                total += 10;
            } else {
                total += Character.getNumericValue(newCard.getCoat());
            }

            // Recheck Ace value adjustment if over 21
            if (hasAce && total > 21) {
                total -= 10;
                hasAce = false; // Reset Ace value to prevent double-counting
            }
        }
    }
}
//endregion
