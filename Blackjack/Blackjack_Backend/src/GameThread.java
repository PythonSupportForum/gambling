import org.java_websocket.WebSocket;

import java.sql.*;
import java.util.*;

// Implementiert das Runnable interface -> Nutzung der Java Implementation für Multithreading
public class GameThread implements Runnable {

    Scanner c = new Scanner(System.in);

    // Liste, aus welcher die Karten entnommen werden, welche dann in den Stacks landen
    List<GameCard> temp = new ArrayList<>();

    final List<GameCard> AVAILABLECARDS = new ArrayList<>();

    boolean running = true;
    WebSocket conn;
    int client_ID;

    List<GameCard> dealerStack = new ArrayList<>();
    List<GameCard> playerStack = new ArrayList<>();
    List<GameCard> playerSplitStack = new ArrayList<>();
    Stack<GameCard> deck = new Stack<>();

    boolean wantsExchange;
    boolean exchangeInput;
    boolean betInput;
    boolean insuranceInput = false;

    boolean cardInput;

    Connection dbConnection;

    int coins = 0;
    double balance = 0.0;
    int bet = 0;
    int splitBet = 0;
    int insuranceBet = 0;
    
    int coinAmount = 0;
    // Ermöglicht Zugriff auf Thread Objekt, wenn GameThread Objekt gefunden wurde
    public Thread currentThread = Thread.currentThread();

    public enum GameState {
        IDLE,
        DEPOSIT,
        START,
        BET,
        SHUFFLE,
        DEALER_START,
        INSURANCE_BET,
        PLAYER_DRAW,
        PLAYER_SPLIT,
        DEALER_DRAW,
        PLAYER_WON,
        PUSH,
        PLAYER_LOST,
        WITHDRAW,
        END
    }

    // Beschreibt den Status, indem sich das Spiel befindet
    GameState gameState = GameState.IDLE;

    // Konstruktor
    public GameThread(int id, WebSocket _conn) {
        client_ID = id;
        conn = _conn;

        dbConnection = getConnection();

        String query = "SELECT Kontostand FROM Kunden WHERE id = " + client_ID;
        try{
            assert dbConnection != null;
            Statement stmt = dbConnection.createStatement();
            stmt.executeQuery(query);
            ResultSet rs = stmt.getResultSet();
            rs.next();
            balance = rs.getDouble("Kontostand");
            rs.close();
            stmt.close();
        }
        catch(SQLException e){}

        //region Karten hinzufügen
        // Clubs (Kreuz)
        AVAILABLECARDS.add(new GameCard('2', 'c'));
        AVAILABLECARDS.add(new GameCard('3', 'c'));
        AVAILABLECARDS.add(new GameCard('4', 'c'));
        AVAILABLECARDS.add(new GameCard('5', 'c'));
        AVAILABLECARDS.add(new GameCard('6', 'c'));
        AVAILABLECARDS.add(new GameCard('7', 'c'));
        AVAILABLECARDS.add(new GameCard('8', 'c'));
        AVAILABLECARDS.add(new GameCard('9', 'c'));
        AVAILABLECARDS.add(new GameCard('0', 'c')); // 10
        AVAILABLECARDS.add(new GameCard('j', 'c')); // Bube
        AVAILABLECARDS.add(new GameCard('q', 'c')); // Dame
        AVAILABLECARDS.add(new GameCard('k', 'c')); // König
        AVAILABLECARDS.add(new GameCard('a', 'c')); // Ass

        // Diamonds (Karo)
        AVAILABLECARDS.add(new GameCard('2', 'd'));
        AVAILABLECARDS.add(new GameCard('3', 'd'));
        AVAILABLECARDS.add(new GameCard('4', 'd'));
        AVAILABLECARDS.add(new GameCard('5', 'd'));
        AVAILABLECARDS.add(new GameCard('6', 'd'));
        AVAILABLECARDS.add(new GameCard('7', 'd'));
        AVAILABLECARDS.add(new GameCard('8', 'd'));
        AVAILABLECARDS.add(new GameCard('9', 'd'));
        AVAILABLECARDS.add(new GameCard('0', 'd')); // 10
        AVAILABLECARDS.add(new GameCard('j', 'd')); // Bube
        AVAILABLECARDS.add(new GameCard('q', 'd')); // Dame
        AVAILABLECARDS.add(new GameCard('k', 'd')); // König
        AVAILABLECARDS.add(new GameCard('a', 'd')); // Ass

        // Hearts (Herz)
        AVAILABLECARDS.add(new GameCard('2', 'h'));
        AVAILABLECARDS.add(new GameCard('3', 'h'));
        AVAILABLECARDS.add(new GameCard('4', 'h'));
        AVAILABLECARDS.add(new GameCard('5', 'h'));
        AVAILABLECARDS.add(new GameCard('6', 'h'));
        AVAILABLECARDS.add(new GameCard('7', 'h'));
        AVAILABLECARDS.add(new GameCard('8', 'h'));
        AVAILABLECARDS.add(new GameCard('9', 'h'));
        AVAILABLECARDS.add(new GameCard('0', 'h')); // 10
        AVAILABLECARDS.add(new GameCard('j', 'h')); // Bube
        AVAILABLECARDS.add(new GameCard('q', 'h')); // Dame
        AVAILABLECARDS.add(new GameCard('k', 'h')); // König
        AVAILABLECARDS.add(new GameCard('a', 'h')); // Ass

        // Spades (Pik)
        AVAILABLECARDS.add(new GameCard('2', 's'));
        AVAILABLECARDS.add(new GameCard('3', 's'));
        AVAILABLECARDS.add(new GameCard('4', 's'));
        AVAILABLECARDS.add(new GameCard('5', 's'));
        AVAILABLECARDS.add(new GameCard('6', 's'));
        AVAILABLECARDS.add(new GameCard('7', 's'));
        AVAILABLECARDS.add(new GameCard('8', 's'));
        AVAILABLECARDS.add(new GameCard('9', 's'));
        AVAILABLECARDS.add(new GameCard('0', 's')); // 10
        AVAILABLECARDS.add(new GameCard('j', 's')); // Bube
        AVAILABLECARDS.add(new GameCard('q', 's')); // Dame
        AVAILABLECARDS.add(new GameCard('k', 's')); // König
        AVAILABLECARDS.add(new GameCard('a', 's')); // Ass
        // endregion
    }

    public GameThread(){
        client_ID = -1;
        conn = null;
        dbConnection = getConnection();

        String query = "SELECT Kontostand FROM Kunden WHERE id = " + client_ID;
        try{
            assert dbConnection != null;
            Statement stmt = dbConnection.createStatement();
            stmt.executeQuery(query);
            ResultSet rs = stmt.getResultSet();
            rs.next();
            balance = rs.getDouble("Kontostand");
            rs.close();
            stmt.close();
        }
        catch(SQLException e){}

        //region Karten hinzufügen
        // Clubs (Kreuz)
        AVAILABLECARDS.add(new GameCard('2', 'c'));
        AVAILABLECARDS.add(new GameCard('3', 'c'));
        AVAILABLECARDS.add(new GameCard('4', 'c'));
        AVAILABLECARDS.add(new GameCard('5', 'c'));
        AVAILABLECARDS.add(new GameCard('6', 'c'));
        AVAILABLECARDS.add(new GameCard('7', 'c'));
        AVAILABLECARDS.add(new GameCard('8', 'c'));
        AVAILABLECARDS.add(new GameCard('9', 'c'));
        AVAILABLECARDS.add(new GameCard('0', 'c')); // 10
        AVAILABLECARDS.add(new GameCard('j', 'c')); // Bube
        AVAILABLECARDS.add(new GameCard('q', 'c')); // Dame
        AVAILABLECARDS.add(new GameCard('k', 'c')); // König
        AVAILABLECARDS.add(new GameCard('a', 'c')); // Ass

        // Diamonds (Karo)
        AVAILABLECARDS.add(new GameCard('2', 'd'));
        AVAILABLECARDS.add(new GameCard('3', 'd'));
        AVAILABLECARDS.add(new GameCard('4', 'd'));
        AVAILABLECARDS.add(new GameCard('5', 'd'));
        AVAILABLECARDS.add(new GameCard('6', 'd'));
        AVAILABLECARDS.add(new GameCard('7', 'd'));
        AVAILABLECARDS.add(new GameCard('8', 'd'));
        AVAILABLECARDS.add(new GameCard('9', 'd'));
        AVAILABLECARDS.add(new GameCard('0', 'd')); // 10
        AVAILABLECARDS.add(new GameCard('j', 'd')); // Bube
        AVAILABLECARDS.add(new GameCard('q', 'd')); // Dame
        AVAILABLECARDS.add(new GameCard('k', 'd')); // König
        AVAILABLECARDS.add(new GameCard('a', 'd')); // Ass

        // Hearts (Herz)
        AVAILABLECARDS.add(new GameCard('2', 'h'));
        AVAILABLECARDS.add(new GameCard('3', 'h'));
        AVAILABLECARDS.add(new GameCard('4', 'h'));
        AVAILABLECARDS.add(new GameCard('5', 'h'));
        AVAILABLECARDS.add(new GameCard('6', 'h'));
        AVAILABLECARDS.add(new GameCard('7', 'h'));
        AVAILABLECARDS.add(new GameCard('8', 'h'));
        AVAILABLECARDS.add(new GameCard('9', 'h'));
        AVAILABLECARDS.add(new GameCard('0', 'h')); // 10
        AVAILABLECARDS.add(new GameCard('j', 'h')); // Bube
        AVAILABLECARDS.add(new GameCard('q', 'h')); // Dame
        AVAILABLECARDS.add(new GameCard('k', 'h')); // König
        AVAILABLECARDS.add(new GameCard('a', 'h')); // Ass

        // Spades (Pik)
        AVAILABLECARDS.add(new GameCard('2', 's'));
        AVAILABLECARDS.add(new GameCard('3', 's'));
        AVAILABLECARDS.add(new GameCard('4', 's'));
        AVAILABLECARDS.add(new GameCard('5', 's'));
        AVAILABLECARDS.add(new GameCard('6', 's'));
        AVAILABLECARDS.add(new GameCard('7', 's'));
        AVAILABLECARDS.add(new GameCard('8', 's'));
        AVAILABLECARDS.add(new GameCard('9', 's'));
        AVAILABLECARDS.add(new GameCard('0', 's')); // 10
        AVAILABLECARDS.add(new GameCard('j', 's')); // Bube
        AVAILABLECARDS.add(new GameCard('q', 's')); // Dame
        AVAILABLECARDS.add(new GameCard('k', 's')); // König
        AVAILABLECARDS.add(new GameCard('a', 's')); // Ass
        // endregion
    }

    // Implementation der run() - Methode des Runnable Interfaces, erste Funktion die nach der Öffnung des Threads ausgeführt wird
    public void run() {
        System.out.print(client_ID + "\n");
        game(); // ruft Hauptmethode des Spiels auf, beginnt Spiel mit dem Client
    }

    // Verarbeiten einer einkommenden Nachricht vom Client
    public void handleMessage(String message) {
        if (message.startsWith("exchange")){
            coinAmount = Integer.parseInt(message.substring(9));
            exchangeInput = true;
        } 
        else if (message.startsWith("bet")) {
            bet = message.substring(7).length();
            betInput = true;
        } else if (message.startsWith("insurance")) {
            insuranceBet = message.substring(11).length();
            insuranceInput = true;
        } else if (message.startsWith("end")) {
            running = false;
        }
    }

    // Funktioniert als Hauptmethode für das Blackjack Spiel
    public void game() {
        //Start der Spiellogik
        setGameState(GameState.START);

        // Loop zum erneuten Spielen
        while (running) {
            setGameState(GameState.DEPOSIT);
            exchangeInput = false;
            betInput = false;
            insuranceInput = false;
            cardInput = false;
            wantsExchange = false;

            playerSplitStack.clear();
            playerStack.clear();
            dealerStack.clear();
            deck.clear();

            System.out.println("Dein Kontostand ist " + balance +" Willst du einzahlen?(true, false)");
            String wants = c.nextLine();

            if(wants.equals("true")){ wantsExchange = true;}

            if(wantsExchange){
                while (!exchangeInput) {
                    //ist nicht vollständig, nach mit Frontend lösen
                    System.out.print("Aktuell du hast " + balance + " TiloTaler\nWie viele Coins willst du erwerben?\n");

                    String inputString = c.nextLine();
                    try {
                        coinAmount = Integer.parseInt(inputString);
                        // Umtauschen: Tilotaler zu Coins
                        if (balance - (coinAmount * 100) < 0) {
                            System.out.println("Du hast nicht genug Tilotaler um diesen Betrag zu erwerben!");
                        } else {
                            coins += coinAmount;
                            balance -= coinAmount * 100;
                            exchangeInput = true;
                            System.out.println("Du hast " + coinAmount + " Coins erworben!");
                        }
                        //endregion
                    } catch (NumberFormatException e) {}
                }
            }
            //region Geld umtauschen


            // Start des Spiels
            setGameState(GameState.START);

            // füllt temp mit allen Karten
            temp.addAll(AVAILABLECARDS);

            setGameState(GameState.BET);
            //region Coins setzen
            while (!betInput) {
                //ist nicht vollständig, nach mit Frontend lösen

                System.out.println("Du hast " + coins + " Coins");
                System.out.println("Wie viele Coins willst du setzen?");

                String inputString = c.nextLine();
                try {
                    bet = Integer.parseInt(inputString);
                    if (bet > coins) {
                        System.out.println("Du hast nicht genug Coins für diesen Betrag!");
                        bet = 0;
                    } else {
                        coins -= bet;
                        System.out.println("Du hast " + bet + " Coins gesetzt!");
                        betInput = true;
                    }
                } catch (NumberFormatException e) {}
            }

            //endregion

            setGameState(GameState.SHUFFLE);

            while (!temp.isEmpty()) {
                Random b = new Random();
                int random = b.nextInt(temp.size());
                deck.push(temp.get(random));
                temp.remove(random);
            }

            setGameState(GameState.DEALER_START);

            dealerStack.add(deck.pop());
            dealerStack.add(deck.pop());
            if (dealerStack.get(1).getValue() == '0' || dealerStack.get(1).getValue() == 'j' || dealerStack.get(1).getValue() == 'q' || dealerStack.get(1).getValue() == 'k') {
                //region Insurance Bet
                setGameState(GameState.INSURANCE_BET);
                while (!insuranceInput) {
                    //ist nicht vollständig, nachher mit Frontend lösen
                    System.out.print("Willst du eine Insurance bet ablegen?(false, true)");

                    Scanner c = new Scanner(System.in);
                    String inputString = c.nextLine();
                    if(inputString.equals("true")){
                        System.out.println("Wieviel?");
                        inputString = c.nextLine();
                        try {
                            insuranceBet = Integer.parseInt(inputString);
                            System.out.println("Du hast " + insuranceBet + " als Insurance Bet gesetzt");
                            insuranceInput = true;
                        } catch (NumberFormatException e) {
                        }
                    }
                    else{
                        insuranceBet = 0;
                        insuranceInput = true;
                    }
                }
                //endregion
            }

            setGameState(GameState.PLAYER_DRAW);

            GameCard card = deck.pop();
            playerStack.add(card);
            printCard(card);

            card = deck.pop();
            playerStack.add(card);
            printCard(card);

            checkValue();
            while (!cardInput) {
                //ist nicht vollständig, nachher mit Frontend lösen
                System.out.println("Willst du noch eine Karte nehmen?(false, true)");

                Scanner c = new Scanner(System.in);
                String inputString = c.nextLine();
                try {
                    if (Boolean.parseBoolean(inputString)) {
                        card = deck.pop();
                        playerStack.add(card);
                        printCard(card);
                    } else {
                        cardInput = true;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
                checkValue();
            }

            if (getGameState() == GameState.PLAYER_DRAW) {

                setGameState(GameState.DEALER_DRAW);

                int total = 0;
                int aceCounter = 0;
                if (dealerStack.get(0).getValue() == 'a') {
                    aceCounter += 1;
                }
                if (dealerStack.get(1).getValue() == 'a') {
                    aceCounter += 1;
                }
                total = currentValue(dealerStack);

                // Weiter Karten nehmen bis eine neue Karte zu riskant ist
                while (total < 17 || (total < 18 && aceCounter > 0)) {
                    GameCard newCard = deck.pop();
                    dealerStack.add(newCard);

                    if (newCard.getCoat() == 'a') {
                        aceCounter += 1;
                        total += 11;
                    } else if (newCard.getCoat() == 'j' || newCard.getCoat() == 'q' || newCard.getCoat() == 'k' || newCard.getCoat() == '0') {
                        total += 10;
                    } else {
                        total += Character.getNumericValue(newCard.getCoat());
                    }

                    // Checken, ob mit dem Ass als 11 die 21 überschritten werden
                    while (aceCounter > 0 && total > 21) {
                        total -= 10;
                        aceCounter -= 1; // Ass Counter einen herabsetzen
                    }
                }

                if (currentValue(dealerStack) < currentValue(playerStack)) {
                    System.out.println("Du hast gewonnen!");
                    // Unser Kontostand muss um den Wert der Coins verringert werden, also Anzahl der Coins * 100
                    setGameState(GameState.PLAYER_WON);
                } else if (currentValue(dealerStack) == currentValue(playerStack)) {
                    System.out.println("Push!");
                    setGameState(GameState.PUSH);
                } else if (currentValue(dealerStack) > currentValue(playerStack)) {
                    System.out.println("Du hast verloren!");
                    setGameState(GameState.PLAYER_LOST);
                } else {
                    System.out.println("Ups??!? Ein Fehler ist aufgetreten!");
                }
                if (insuranceBet > 0 && dealerStack.get(0).getValue() == 'a') {
                    coins += insuranceBet;
                    System.out.println("Du hast den Insurance Bet erhalten!");
                    setGameState(GameState.PLAYER_LOST);
                }
            }

            if (getGameState() == GameState.PLAYER_WON){
                // Spieler gewinnt, der Spieler erhält seinen Einsatz im Verhältnis 2:1 zurück
                coins += 2 * bet;
                System.out.println("Du hast " + bet + " Coins gewonnen");
            } else if (getGameState() == GameState.PLAYER_LOST) {
                // Spieler verliert, es passiert nichts
                System.out.println("Du hast " + bet + " Coins verloren!");
            }
            else if (getGameState() == GameState.PUSH) {
                // Push, der Spieler erhält seine Coins zurück
                coins += bet;
                System.out.println("Du hast " + bet + " Coins zurück erhalten!");
            }
        }

        setGameState(GameState.WITHDRAW);

        setGameState(GameState.END);

        conn.close(); // Verbindung beenden
        currentThread.interrupt();// Beende den Thread
    }

    private void checkValue() {
        if (currentValue(playerStack) > 21) {
            setGameState(GameState.PLAYER_LOST);
            System.out.println("Bust! Du hast verloren!");
            cardInput = true;
        }
        else if (currentValue(playerStack) == 21) {
            System.out.println("Herzlichen Glückwunsch! Du hast einen Blackjack!");
            setGameState(GameState.PLAYER_WON);
            cardInput = true;
        }
    }

        public int currentValue (List<GameCard> playerStack) {
            int totalValue = 0;
            int aceCount = 0;

            for (GameCard card : playerStack) {
                char valueOfCard = card.getValue();

                if (valueOfCard >= '2' && valueOfCard <= '9') {
                    // Numerische Karten: '2' bis '9'
                    totalValue += Character.getNumericValue(valueOfCard);
                } else if (valueOfCard == '0' || valueOfCard == 'j' || valueOfCard == 'q' || valueOfCard == 'k') {
                    // Zehner ('0'), Bube ('j'), Dame ('q'), König ('k'): Wert 10
                    totalValue += 10;
                } else if (valueOfCard == 'a') {
                    // Ass: Hat zunächst den Wert 11
                    totalValue += 11;
                    aceCount++;
                }
            }

            // Wenn der Gesamtwert > 21 ist, wird der Wert der Asse reduziert (11 → 1)
            while (totalValue > 21 && aceCount > 0) {
                totalValue -= 10; // Ein Ass wird von 11 auf 1 reduziert
                aceCount--;       // Ein Ass weniger mit Wert 11
            }

            return totalValue;
        }

        //region Getter and Setter
        public GameState getGameState () {
            return gameState;
        }

        public void setGameState (GameState gameState){
            this.gameState = gameState;
        }
        //endregion

        void printCard(GameCard card){
        String coat = "";
            System.out.print("Wert: " + card.getValue() + " ");
            switch (card.getCoat()){
                case 'c':
                    coat = "Clubs";
                    break;
                case 'd':
                    coat = "Diamonds";
                    break;
                case 'h':
                    coat = "Hearts";
                    break;
                case 's':
                    coat = "Spades";
                    break;
                default:
                    break;
            }
            System.out.print("Farbe: " + coat + "\n");
        }

    // Methode zum Erstellen der Verbindung
    public static Connection getConnection() {
        // Verknüpfung zur Datenbank
        String url = "jdbc:mariadb://db.ontubs.de:3306/gambling";
        // Benutzername und Passwort
        String user = "carl";
        String password = "geilo123!";

        try {
            // Verbindung herstellen
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Verbindung zur Datenbank erfolgreich!");
            return connection;
        } catch (SQLException e) {
            // Fehlerbehandlung, falls die Verbindung fehlschlägt
            System.err.println("Datenbankverbindung fehlgeschlagen!");
            e.printStackTrace();
            return null;
        }
    }
}