import org.java_websocket.WebSocket;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

// Implementiert das Runnable interface -> Nutzung der Java Implementation für Multithreading
public class GameThread implements Runnable {

    Scanner c = new Scanner(System.in);

    // Liste, aus welcher die Karten entnommen werden, welche dann in den Stacks landen
    List<GameCard> temp = new ArrayList<>();

    final List<GameCard> AVAILABLECARDS = new ArrayList<>();
    final double OLDBALANCE;

    boolean running = true;
    WebSocket conn;
    int client_ID;

    List<GameCard> dealerStack = new ArrayList<>();
    ArrayList<ArrayList<GameCard>> playerStack = new ArrayList<>();
    HashMap<Integer, StackState> states;
    Stack<GameCard> deck = new Stack<>();
    GameCard card;

    Statement stmt;

    boolean start = false;
    boolean wantsExchange;
    boolean exchangeInput;
    boolean betInput;
    String askInput = ""; //Allgemein für Nachfragen ans Frontent
    boolean insuranceInput = false;
    boolean inputWait = true;
    boolean doubleDown = false;
    boolean playerDone = false;
    boolean waitDoubleDown = false;

    boolean[] cardInput = new boolean[4];

    Connection clientDB;
    Connection transactionDB;

    int chips = 0;
    double balance = 0.0;
    int bet = 0;
    boolean doubleDownInput = false;
    boolean askDealer = false;
    boolean askForResult = false;
    boolean splitInput = false;
    int takeCount = 0;
    int splitCount = 0;
    int insuranceBet = 0;

    int chipAmount = 0;

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
        GAME_END,
        WITHDRAW,
        END
    }
    public enum StackState {
        RUNNING,
        WON,
        LOST,
        PUSH
    }

    // Beschreibt den Status, indem sich das Spiel befindet
    GameState gameState = GameState.IDLE;

    // Konstruktor
    public GameThread(String token, WebSocket _conn) {
        System.out.println("Token: "+token);
        conn = _conn;

        Connection clientDB = getConnection();
        String query = "SELECT Kunden.*, SUM(t.Betrag) as Kontostand FROM Kunden " +
                "JOIN Transaktionen as t ON t.Kunden_ID = Kunden.id " +
                "WHERE Kunden.token = ?";

        try (PreparedStatement stmt = clientDB.prepareStatement(query)) {
            stmt.setString(1, token); // Token als sicherer Parameter

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) { // Prüfen, ob es ein Ergebnis gibt
                    balance = rs.getDouble("Kontostand");
                    client_ID = rs.getInt("id"); // Client ID aus Datenbank extrahieren
                    System.out.println("Balance: " + balance + ", Client ID: " + client_ID);
                } else {
                    System.out.println("Kein Kunde mit diesem Token gefunden.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        OLDBALANCE = balance;

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

    // Test Konstruktor zum Debuggen
    public GameThread(){
        client_ID = -1;
        conn = null;
        clientDB = getConnection();

        String query = "SELECT SUM(Betrag) as Kontostand FROM Transaktionen WHERE Kunden_ID = " + client_ID;
        try{
            assert clientDB != null;
            stmt = clientDB.createStatement();
            stmt.executeQuery(query);
            ResultSet rs = stmt.getResultSet();
            rs.next();
            balance = rs.getDouble("Kontostand");
            rs.close();
            stmt.close();
        }
        catch(SQLException e){}

        OLDBALANCE = balance;

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
        // Warte auf Startsignal vom Client
        while(!start){
            try{
                Thread.sleep(200);
            }catch(Exception ignored){}
        }
        game(); // ruft Hauptmethode des Spiels auf, beginnt Spiel mit dem Client
    }

    //Um den Text anzuzeigen im Frontend der i Ergebiss popup seht
    public void sendGameResultText(String t) {
        conn.send("text:"+t);
    }

    // Verarbeiten einer einkommenden Nachricht vom Client
    public void handleMessage(String message) {
        System.out.println("Nachricht von Client " + client_ID + " empfangen: " + message + "\n");
        if(message.contains(":")){
            if (message.startsWith("exchange:")){
                chipAmount = Integer.parseInt(message.substring("exchange:".length()).trim());
                System.out.println("Client " + client_ID + " will " + chipAmount + " umtauschen\n");
                if ((balance - (double) (chipAmount / 100)) < 0) {
                    System.out.println("Hat nicht genug Tilotaler um diesen Betrag zu erwerben!");
                    conn.send("ChipUpdate:-1");
                } else {
                    chips += chipAmount;
                    balance -= (double) chipAmount / 100;
                    exchangeInput = true;
                    System.out.println("Client " + client_ID + " hat " + chipAmount + " Coins erworben!");
                    conn.send("ChipUpdate:" + chips);
                }
            }
            else if (message.startsWith("bet:")) {
                bet = Integer.parseInt(message.substring("bet:".length()).trim());
                System.out.println("Client " + client_ID + " wettet " + bet);
                if(bet > chipAmount){
                    bet = chipAmount;
                }
                chips -= chipAmount;
                betInput = true;
            }
            else if (message.startsWith("answer:")) {
                askInput = message.substring("answer:".length());
            }
            else if (message.startsWith("endstack:")) {
                int i = Integer.parseInt(message.substring("endstack:".length()));
                System.out.println("Got End Stack! " + i);
                cardInput[i] = true;
            }
        }
        else{
            switch(message.toLowerCase()){
                case "takedealer":
                    takeCount += 1;
                    inputWait = false;
                    break;
                case "getresult":
                    askForResult = true;
                    break;
                case "split":
                    inputWait = false;
                    splitInput = true;
                    break;
                case "getdealer":
                    playerDone = true;
                    inputWait = false;
                    askDealer = true;
                    break;
                case "takeuser":
                    takeCount += 1;
                    inputWait = false;
                    waitDoubleDown = false;
                    break;
                case "end":
                    running = false;
                    break;
                case "start":
                    start = true;
                    DecimalFormat df = new DecimalFormat("0.00");
                    conn.send("Bal:"+ df.format(balance));
                    System.out.println("Bal:"+balance);
                    break;
                default:
                    System.out.println("ERROR: Message not detected");
                    break;
            }
        }
    }

    public void sendChipCount(int c) {
        this.askFrontend("coins:"+c);
    }

    public String askFrontend(String query) { // Variablenminimierung
        askInput = ""; // Reset
        conn.send("ask:"+query);
        while(askInput.length() == 0) {
            try {
                Thread.sleep(200);
            }
            catch (Exception ignored) {}
        }
        System.out.println("Frontent Answer:"+askInput);
        return askInput;
    }

    // Funktioniert als Hauptmethode für das Blackjack Spiel
    public void game() {
        System.out.println("Start des Spiels");
        //Start der Spiellogik
        setGameState(GameState.START);
        states = new HashMap<>();

        ArrayList<GameCard> temp = new ArrayList<>();
        playerStack.add(temp);
        states.put(0, StackState.RUNNING);

        // Loop zum erneuten Spielen
        while (running) {
            setGameState(GameState.DEPOSIT);
            exchangeInput = false;
            betInput = false;
            insuranceInput = false;
            cardInput[0] = false;
            cardInput[1] = false;
            cardInput[2] = false;
            cardInput[3] = false;
            inputWait = true;
            wantsExchange = false;
            doubleDown = false;
            playerDone = false;
            askDealer = false;
            takeCount = 0;
            for(int i = 0; i < states.size(); i++){
                states.replace(i, StackState.RUNNING);
            }

            for(ArrayList<GameCard> stack : playerStack){
                stack.clear();
            }
            dealerStack.clear();
            deck.clear();

            sendChipCount(chips);

            if(chips == 0 && balance == 0){
                running = false;
                System.out.println("Kein Geld mehr");
                continue;
            }

            // Start des Spiels
            setGameState(GameState.START);

            // füllt temp mit allen Karten
            temp.addAll(AVAILABLECARDS);

            setGameState(GameState.BET);
            //region Coins setzen
            while (!betInput) {
                //ist nicht vollständig, nach mit Frontend lösen
                try {
                    Thread.sleep(200);
                }
                catch (Exception ignored) {}
            }

            setGameState(GameState.SHUFFLE);

            while (!temp.isEmpty()) {
                Random b = new Random();
                int random = b.nextInt(temp.size());
                deck.push(temp.get(random));
                temp.remove(random);
            }

            setGameState(GameState.DEALER_START);

            while(takeCount < 1){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            GameCard tempCard = deck.pop();
            printCard(tempCard);
            dealerStack.add(tempCard);

            card = deck.pop();
            int currentValue = currentValue(dealerStack);
            dealerStack.add(card);
            conn.send("DealerCard:c:" + card.getCoat() + ",v:" + card.getValue() + ",p:" + (currentValue(dealerStack) - currentValue));
            printCard(card);
            takeCount--;

            if (dealerStack.get(1).getValue() == 'a') {
                //region Insurance Bet
                setGameState(GameState.INSURANCE_BET);

                System.out.print("Willst du eine Insurance bet ablegen?(false, true)\n");

                String a = askFrontend("insurance");
                String[] r = a.split(";");
                if(Objects.equals(r[0], "true")) {
                    insuranceInput = true;
                    insuranceBet = Integer.parseInt(r[1]);
                    System.out.println("Du hast " + insuranceBet + " als Insurance Bet gesetzt");
                } else {
                    insuranceInput = true;
                    insuranceBet = 0;
                }
                //endregion
            }

            setGameState(GameState.PLAYER_DRAW);

            while(takeCount < 2){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            for(int i = 0; i < 2 ; i++){
                card = deck.pop();
                currentValue = currentValue(playerStack.get(0));
                playerStack.get(0).add(card);
                conn.send("Card:c:" + card.getCoat() + ",v:" + card.getValue() + ",p:" + (currentValue(playerStack.get(0)) - currentValue));
                printCard(card);
                takeCount--;
            }

            waitDoubleDown = true;
            String a = askFrontend("double");
            String[] r = a.split(";");
            if(Objects.equals(r[0], "true")) {
                System.out.println("Double Down!");

                while(waitDoubleDown){
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                card = deck.pop();
                conn.send("Card:c:" + card.getCoat() + ",v:" + card.getValue() + ",p:" + (currentValue(playerStack.get(0)) - currentValue));
                playerStack.get(0).add(card);
                printCard(card);
                balance -= bet;
                bet *= 2;
                inputWait = false;
                doubleDown = true;
                int ended = 0;
                for(int i = 0; i < splitCount + 1; i++){
                    if (states.get(i) != StackState.RUNNING) {
                        ended++;
                    }
                }
                if(splitCount == ended) setGameState(GameState.GAME_END);
                System.out.println("Spieler fertig");
                if(getGameState() == GameState.GAME_END) System.out.println("Ended early");
                while(cardInput[0] != true){
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                checkValue(0);
                setGameState(GameState.DEALER_DRAW);
            } else {
                System.out.println("Der Spieler hat kein Double Down gesetzt!");
            }

            if (getGameState() == GameState.PLAYER_DRAW) {
                // Main Split Logic
                splitCheck(0);
                System.out.println("Checked Split! " + splitCount);
                for (int i = 0; i <= splitCount; i++) {
                    playerDone = false;
                    karteZiehen(i); // Spieler zieht pro gesplittetes Deck
                }
                System.out.println("Alle Karten gezogen! " + cardInput[0] + " " + playerDone);
                while (!playerDone) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                int ended = 0;
                for(int i = 0; i < splitCount + 1; i++){
                    if (states.get(i) != StackState.RUNNING) {
                        ended++;
                    }
                }
                if(splitCount == ended) setGameState(GameState.GAME_END);
                System.out.println("Spieler fertig");
                if(getGameState() == GameState.GAME_END) System.out.println("Ended early");
            }
            if(getGameState() != GameState.GAME_END) {
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

                    if (newCard.getValue() == 'a') {
                        aceCounter += 1;
                        total += 11;
                    } else if (newCard.getValue() == 'j' || newCard.getValue() == 'q' || newCard.getValue() == 'k' || newCard.getValue() == '0') {
                        total += 10;
                    } else {
                        total += Character.getNumericValue(newCard.getValue());
                    }

                    // Checken, ob mit dem Ass als 11 die 21 überschritten werden
                    while (aceCounter > 0 && total > 21) {
                        total -= 10; // Ass Wert auf 1 anpassen
                        aceCounter -= 1; // Ass Counter einen herabsetzen
                    }
                }
            }
            System.out.println("In");
            while(!askDealer){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("Out");
            String sendDealer = "DealerCards:";
            for (GameCard card : dealerStack) {
                sendDealer += "c:" + card.getCoat() + ",v:" + card.getValue() + ";";
            }
            conn.send(sendDealer.substring(0, sendDealer.length() - 1) + ">" + currentValue(dealerStack));
            System.out.println(sendDealer.substring(0, sendDealer.length() - 1) + ">" + currentValue(dealerStack));
            //Methode zur Bestimmung wer gewonnen oder verloren hat nach unten ausgelagert
            checkGameState();

            System.out.println("Ask Frontend for End!");

            String askEnd = askFrontend("end");
            running = !askEnd.equals("true");
        }

        setGameState(GameState.WITHDRAW);

        if (balance == 0 && chips == 0){
            updateBalance(0);
        }
        else{
            while (true) {
                System.out.println("Du hast " + chips +" Coins\nWie viele Coins willst du in Tilotaler umwandeln?");
                try {
                    int input = Integer.parseInt(c.nextLine());
                    if(updateBalance(input)){
                        break;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        setGameState(GameState.END);
        handleQuit();// Kümmert sich um die ausstehende Verbindung, falls diese aktiv sind
        currentThread.interrupt(); // Beende den Thread
    }

    private void checkValue(int index) {
        ArrayList<GameCard> cardStack = playerStack.get(index);
        if (currentValue(cardStack) > 21) {
            states.replace(index, StackState.LOST);
            System.out.println("Bust! Du hast auf Stapel " + (index + 1) + " verloren!");
            conn.send("bust:"+index);
            cardInput[index] = true;
            sendChipCount(chips);
            playerDone = true;
        }
        else if (currentValue(cardStack) == 21) {
            System.out.println("Herzlichen Glückwunsch! Du hast auf Stapel " + (index + 1) + " einen Blackjack!");
            conn.send("blackjack:"+index);
            this.sendGameResultText("Herzlichen Glückwunsch! Du hast auf Stapel " + (index + 1) + " einen Blackjack!\n");
            states.replace(index, StackState.WON);
            cardInput[index] = true;
            sendChipCount(chips);
            playerDone = true;
        }
    }

    public int currentValue (List<GameCard> cardStack) {
        int totalValue = 0;
        int aceCount = 0;

        for (GameCard card : cardStack) {
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

    public void karteZiehen(int index){
        while (!cardInput[index]) {
            while(inputWait){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                for(; takeCount > 0; takeCount--) {
                    card = deck.pop();
                    int j = currentValue(playerStack.get(index));
                    playerStack.get(index).add(card);
                    conn.send("Card:c:" + card.getCoat() + ",v:" + card.getValue() + ",p:" + (currentValue(playerStack.get(0)) - j));
                    printCard(card);
                }
            } catch (NumberFormatException e) {
                continue;
            }
            checkValue(index);
        }
    }

    public void splitCheck(int index){
        if(playerStack.get(index).get(0).getValue() == playerStack.get(index).get(1).getValue()){
            String a = askFrontend("split");
            String[] r = a.split(";");
            if(Objects.equals(r[0], "true")) {
                System.out.println("Frontend Möchte Splitten");
                ArrayList<GameCard> temp = new ArrayList<>();
                playerStack.add(temp);
                playerStack.get(index + 1).add(playerStack.get(index).get(1));
                playerStack.get(index).remove(1);
                splitCount++;
                states.replace(index, StackState.RUNNING);
            } else System.out.println("Frontend möchte nicht splitten!");
        }
    }
    public void checkGameState(){
        StringBuilder t = new StringBuilder(); //Text wird gesammelt mit allen Ergebnissen!
        if (insuranceBet > 0 && dealerStack.get(0).getValue() == '0') {
            chips += insuranceBet;
            t.append("Du hast den Insurance Bet erhalten!\n");
        }
        for(int i = 0; i <= splitCount; i++) {
            ArrayList<GameCard> a = playerStack.get(i);
            if(states.get(i) == StackState.RUNNING){
                if(currentValue(a) > 21){

                }
                else if (currentValue(a) == 21){

                }
                else if(currentValue(dealerStack) > 21){
                    states.replace(i, StackState.WON);
                } else if (currentValue(dealerStack) < currentValue(a) && currentValue(a) < 22) {
                    System.out.println("Player higher than dealer");
                    states.replace(i, StackState.WON);
                } else if(currentValue(dealerStack) == currentValue(a) && currentValue(a) < 22){
                    states.replace(i, StackState.PUSH);
                } else if (currentValue(dealerStack) > currentValue(a) && currentValue(dealerStack) < 22) {
                    states.replace(i, StackState.LOST);
                    System.out.println("Player lower than dealer");
                } else {
                    t.append("Ups??!? Ein Fehler ist aufgetreten! 1\n");
                }
            }
            if(states.get(i) == StackState.WON) {
                t.append("Auf Stapel " + (i + 1) + " hast du gewonnen!\n");
                chips += 2 * bet;
                t.append("Du hast " + bet + " Coins gewonnen\n");
            } else if (states.get(i) == StackState.PUSH) {
                t.append("Push auf Stapel " + (i + 1) + "!\n");
                chips += bet;
                t.append("Du hast " + bet + " Coins zurück erhalten!\n");
            }
            else if (states.get(i) == StackState.LOST) {
                t.append("Du hast auf Stapel " + (i + 1) + " verloren!\n");
                t.append("Du hast " + bet + " Coins verloren!\n");
            }
            else {
                t.append("Ups??!? Ein Fehler ist aufgetreten! 2\n");
            }
        }
        System.out.println(t); //Alles Ausgeben
        while(!askForResult){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        sendGameResultText(t + ">"+chips); //send ans Frontent den Text als Ergebisse
        setGameState(GameState.GAME_END);
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

    public boolean updateBalance(int coinAmount){
        if (coinAmount > chips) {
            System.out.println("Du hast nicht genug Coins!");
            return false;
        } else {
            chips -= coinAmount;
            sendChipCount(chips);

            // Formatieren Sie den Betrag korrekt
            double amount = balance - OLDBALANCE;
            String formattedAmount = new DecimalFormat("0.00").format(amount);

            // Erstellen Sie die SQL-Abfrage
            String transactionQuery = "INSERT INTO Transaktionen (Kunden_ID, Betrag, type) VALUES ("
                    + client_ID + ", "
                    + formattedAmount + ", "
                    + "'blackjack')"; // type wird explizit gesetzt

            clientDB = getConnection();
            try {
                // Verbindung zur Datenbank, Veränderung des Kontostandes
                stmt = clientDB.createStatement();
                stmt.executeUpdate(transactionQuery);
                stmt.close();
                clientDB.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            System.out.println("Du hast " + coinAmount + " Coins umgewandelt!");
            return true;
        }
    }

    public void handleQuit(){
        if(running){
            updateBalance(chips);
            running = false;
        }
        if(conn != null){
            conn.close();
        }
    }
}