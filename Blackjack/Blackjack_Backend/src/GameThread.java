import org.java_websocket.WebSocket;

import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;

// Implementiert das Runnable interface -> Nutzung der Java Implementation für Multithreading
public class GameThread implements Runnable {
    // Statische Liste, die alle Karten eines Pokerdecks enthält
    final List<GameCard> AVAILABLECARDS = new ArrayList<>();
    //Statischer, erster Wert des Kontostandes, wichtig zur Berechnung der Transaktionen zu Ende des Programms
    final double OLDBALANCE;

    boolean running = true; // Bestimmt, ob das Programm läuft
    WebSocket conn; // Websocket Verbindung mit dem Frontend, Verknüpfung zum User Interface
    int client_ID; // ID des Kunden, wichtig zur Zuordnung in der Datenbank

    List<GameCard> dealerStack = new ArrayList<>(); // Stapel an Karten, die der Dealer im Verlauf des Spiels zieht
    ArrayList<ArrayList<GameCard>> playerStack = new ArrayList<>(); /* Der Spieler kann mithilfe von Splits 4 unterschiedliche Stapel an Karten haben.
    Dass es dazu kommt, ist extrem unwahrscheinlich, die Möglichkeit besteht jedoch. Dieses Array beinhaltet jedes dieser 4 Stapel. Mindestens einer wird pro Spielrunde verwendet.
    Der Rest erhält den Wert null*/
    HashMap<Integer, StackState> states; // Ein Dictionary um den Status eines dieser Stapel mithilfe seiner ID festzuhalten und unabhängig von den anderen zu verändern
    Stack<GameCard> deck = new Stack<>(); // Das Deck von dem gezogen wird
    GameCard card; // Ein Objekt der Klasse GameCard. Repräsentiert eine Spielkarte

    Statement stmt; // SQL-Statement Objekt

    // Variablen, mit denen auf das Frontend gewartet wird
    boolean start = false;
    boolean wantsExchange;
    boolean exchangeInput;
    boolean askDealer = false;
    boolean askForResult = false;
    boolean splitInput = false;
    boolean betInput;
    String askInput = ""; //Allgemein für Nachfragen ans Frontend
    boolean insuranceInput = false;
    boolean inputWait = true;
    boolean doubleDown = false;
    boolean playerDone = false;
    boolean waitDoubleDown = false;
    int takeCount = 0;

    boolean[] cardInput = new boolean[4];

    Connection gamingDB; // Objekt zur Anbindung an die Datenbank

    int chips = 0; // Kontostand in Spiel Jetons
    double balance = 0.0; // Allgemeiner Kontostand in TiloTalern
    int bet = 0; // Betrag, den der Spieler gesetzt hat
    int splitCount = 0; // Anzahl, wie oft gesplittet wurde, wichtig um den richtig Stack beim splitten aufzurufen
    int insuranceBet = 0; // Betrag, den der Spieler als Insurance Bet setzt

    int chipAmount = 0; //temporär, Uebermittlung Frontend

    // Ermöglicht Zugriff auf Thread Objekt, wenn GameThread Objekt gefunden wurde
    public Thread currentThread = Thread.currentThread();

    // Ansammlung aller Zustaende, in denen sich das Spiel und jeder einzelne Stapel des Spielers aufhalten kann
    public enum GameState { // Alle Zustände, in denen sich das Spiel befinden kann, wichtig für Synchronisation mit Frontend
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
    public enum StackState { // Jeder Zustand, den ein Stapel Karten haben kann; wichtig fuer differenzierte Auswertung beim splitten
        RUNNING,
        WON,
        LOST,
        PUSH
    }

    // Beschreibt den Status, indem sich das Spiel befindet
    GameState gameState = GameState.IDLE;

    // Konstruktor, der durch BlackjackServer aufgerufen wird
    public GameThread(String token, WebSocket _conn) {
        System.out.println("Token: "+token);
        conn = _conn;

        // Datenbankverbindung und Auslese aller relevanten Informationen
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

        // Setzen des statischen Wertes
        OLDBALANCE = balance;

        //Hinzufügen aller Karten zur statischen Liste AVAILABLECARDS
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

    /** Test Konstruktor zum Debuggen
    public GameThread(){
        client_ID = -1;
        conn = null;
        gamingDB = getConnection();

        String query = "SELECT SUM(Betrag) as Kontostand FROM Transaktionen WHERE Kunden_ID = " + client_ID;
        try{
            assert gamingDB != null;
            stmt = gamingDB.createStatement();
            stmt.executeQuery(query);
            ResultSet rs = stmt.getResultSet();
            rs.next();
            balance = rs.getDouble("Kontostand");
            rs.close();
            stmt.close();
        }
        catch(SQLException ignored){}

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
    */

    // Implementation der run() - Methode des Runnable Interfaces, erste Funktion die nach der Öffnung des Threads durch BlackjackServer ausgeführt wird
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

    // region Game Logic
    // funktioniert als Hauptmethode für das Blackjack Spiel
    public void game() {
        System.out.println("Start des Spiels");
        //Start der Spiellogik
        setGameState(GameState.START);
        states = new HashMap<>(); // initialisieren des Dictionaries, in dem der Status des Stapels dem Stapel des Spielers zugeordnet wird

        ArrayList<GameCard> temp = new ArrayList<>();
        playerStack.add(new ArrayList<>()); // In die zweidimensionale ArrayListe wird eine neue Liste als Kartenstapel hinzufügenen
        states.put(0, StackState.RUNNING); // Der Stack ist aktuell am laufen

        // Loop zum erneuten Spielen
        while (running) {
            // Alle wichtigen Variablen, die pro Spiel immer wieder zurueckgesetzt, bekommen hier ihren Ursprungswert
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
            for (int i = 0; i < states.size(); i++) {
                states.replace(i, StackState.RUNNING);
            }

            for (ArrayList<GameCard> stack : playerStack) {
                stack.clear();
            }
            dealerStack.clear();
            deck.clear();

            sendChipCount(chips);

            if (chips == 0 && balance == 0) { // Ueberpruefung, ob der Spieler genug Geld hat
                running = false;
                System.out.println("Kein Geld mehr");
                continue;
            }

            // Start des Spiels
            setGameState(GameState.START);

            // fuellt temp mit allen Karten
            temp.addAll(AVAILABLECARDS);

            setGameState(GameState.BET);
            try {
                Thread.sleep(200);
            } catch (Exception ignored) {}

            System.out.println("Bet");
            askFrontend("bet"); // Das Backend Programm wartet darauf, dass der Spieler eine Wette gesetzt hat
            bet = Integer.parseInt(askInput.substring("bet:".length()).trim());
            System.out.println("Client " + client_ID + " wettet " + bet);
            if (bet > chips) {
                bet = chips;
            }
            chips -= bet;

            System.out.println("Bet done");

            setGameState(GameState.SHUFFLE);
            // Die Karten werden mittels der Random klasse, gemischt auf den Nachziehstapel (deck) getan
            while (!temp.isEmpty()) {
                Random b = new Random();
                int random = b.nextInt(temp.size());
                deck.push(temp.get(random));
                temp.remove(random);
            }

            setGameState(GameState.DEALER_START);

            while (takeCount < 1) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            card = deck.pop(); // Von Nachzeihstapel werden zweiKarten gezogen, ausgegeben, auf den DealerStack gelegt und an das Frontend gesendet
            printCard(card);
            dealerStack.add(card);
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
                if (Objects.equals(r[0], "true")) {
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

            while (takeCount < 2) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            for (int i = 0; i < 2; i++) { // Der Spieler bekommt zwei Karten aufgedeckt
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
            if (Objects.equals(r[0], "true")) { // Wird nur ausgeführt, wenn der Spieler einen Double Down machen will
                System.out.println("Double Down!");

                while (waitDoubleDown) {
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
                for (int i = 0; i < splitCount + 1; i++) {
                    if (states.get(i) != StackState.RUNNING) {
                        ended++;
                    }
                }
                if (splitCount == ended) setGameState(GameState.GAME_END);
                System.out.println("Spieler fertig");
                if (getGameState() == GameState.GAME_END) System.out.println("Ended early");
                while (cardInput[0] != true) {
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

            if (getGameState() == GameState.PLAYER_DRAW) { // Normales Programm, wird nur ausgeführt, wenn vorher kein Double Down passiert ist
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
                for (int i = 0; i < splitCount + 1; i++) {
                    if (states.get(i) != StackState.RUNNING) {
                        ended++;
                    }
                }
                if (splitCount == ended)
                    setGameState(GameState.GAME_END); // Nur wenn alle Stapel beendet wurden, Bsp. Bust/Blackjack, kann das Spiel an sich beendet werden
                System.out.println("Spieler fertig");
                if (getGameState() == GameState.GAME_END) System.out.println("Ended early");
            }

            if (getGameState() != GameState.GAME_END) { // Wenn das Spiel noch nicht vorbei ist, macht der Dealer nach dem Spieler weiter
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

                // Der Dealer geht nur bis zum einen Stapelwert von 17 mit
                while (total < 18) {
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
            while (!askDealer) {
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

        if (balance == 0 && chips == 0) {
            updateBalance(0);
        } else {
            System.out.println("Withdrawal time");
            while (true) { // Provisorisch
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        setGameState(GameState.END);
        handleQuit();// Kuemmert sich um die ausstehende Verbindung, falls diese aktiv sind
        currentThread.interrupt(); // Beende den Thread

    }

    private void checkValue(int index) { // Die Methode ueberprueft nur auf Blackjack oder Bust, das ist wichtig, weil das auch waehrend des Spiels passieren kann, dieses muss dann frühzeitig verwendet werden
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

    public int currentValue (List<GameCard> cardStack) { // Die Methode berechnet generell den Wert einer Stacks, dabei wird beruecksichtigt, dass ein Ass situationsbedingt 1 oder 11 sein kann
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

    void printCard(GameCard card){ // Die Methode gibt eine Karte im Backend in der konsole aus, um zu debuggen
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

    public void karteZiehen(int index){ // Die Methode wurde ausgelagert, da diese an verschiedenen Stellen in Programm benoetigt wird, mit dem index kann vorgegeben werden auf welchen PlayerStack die Karte gezogen wird
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
                    conn.send("Card:c:" + card.getCoat() + ",v:" + card.getValue() + ",p:" + (currentValue(playerStack.get(index)) - j));
                    printCard(card);
                }
            } catch (NumberFormatException e) {
                continue;
            }
            checkValue(index);
        }
    }

    public void splitCheck(int index){ // Die Methode wurde ausgelagert, immer wenn diese waehrend des Spielablaufes aufgerufen wird, ueberprueft sie ob gesplittet werden kann und ob der Spieler das will; wenn das der Fall ist, wird einer neuer Stapel erstellt und ei Karten werden entsprechend aufgeteilt
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
    public void checkGameState(){ // Die Methode wurde ausgelagert, wenn diese aufgerufen wird (einmal am Ende), werden die Ergebnisse aller Stapel ausgerechnet und übergeben
        StringBuilder t = new StringBuilder(); //Text wird gesammelt mit allen Ergebnissen!
        if (insuranceBet > 0 && dealerStack.getFirst().getValue() == '0') {
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
        sendGameResultText(t + ">"+chips); // Resultat wird ans Frontend gesendet
        setGameState(GameState.GAME_END);
    }
    //endregion

    // region Database Connection
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

    // Methode, um den durch das Spiel geänderten Kontostand zu aktualisieren
    public boolean updateBalance(int coinAmount){
        if (coinAmount > chips) {
            System.out.println("Du hast nicht genug Coins!"); // Falls Falscheingabe durch Client
            return false;
        } else {
            balance += coinAmount / 100;
            chips -= coinAmount;
            sendChipCount(chips); // Aktualisierung im Frontend

            // Formatieren des Betrags
            double amount = balance - OLDBALANCE;
            String formattedAmount = new DecimalFormat("0.00").format(amount);

            // Erstellen Sie die SQL-Abfrage
            String transactionQuery = "INSERT INTO Transaktionen (Kunden_ID, Betrag, type) VALUES ("
                    + client_ID + ", "
                    + formattedAmount + ", "
                    + "'blackjack')"; // type wird explizit gesetzt

            gamingDB = getConnection();
            try {
                // Verbindung zur Datenbank, Veränderung des Kontostandes
                stmt = gamingDB.createStatement();
                stmt.executeUpdate(transactionQuery);
                stmt.close();
                gamingDB.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            System.out.println("Du hast " + coinAmount + " Coins umgewandelt!");
            return true; // Rückgabe, wenn Aktualisierung gelingt
        }
    }
    // endregion

    // region Frontend Connection
    public void sendChipCount(int c) {
        this.askFrontend("coins:"+c); // übermitteln des Kontostands an Jetons mithilfe des Befehls "coins:"
    }

    public String askFrontend(String query) { // Variablenminimierung, Anfrage an das Frontend mit anschliessendem Warten auf die Antwort
        askInput = ""; // Reset
        conn.send("ask:"+query);
        while(askInput.length() == 0) {
            try {
                Thread.sleep(200);
            }
            catch (Exception ignored) {}
        }
        System.out.println("Frontend Answer:"+askInput);
        return askInput;
    }

    // Debugging Lösung zur Angabe des Ergebnisses des Spiels
    public void sendGameResultText(String t) {
        conn.send("text:"+t);
    }

    // Verarbeiten einer einkommenden Nachricht vom Client
    public void handleMessage(String message) {
        System.out.println("Nachricht von Client " + client_ID + " empfangen: " + message + "\n");
        if(message.contains(":")){ // alle, die ein ':' enthalten, enthalten Werte die gelesen werden müssen. Simple Befehle vom Client können durch ein switch-case bearbeitet werden
            if (message.startsWith("exchange:")){
                chipAmount = Integer.parseInt(message.substring("exchange:".length()).trim());
                System.out.println("Client " + client_ID + " will " + chipAmount + " umtauschen\n");
                if ((balance - (double) (chipAmount * 100)) < 0) {
                    System.out.println("Hat nicht genug Tilotaler um diesen Betrag zu erwerben!");
                    conn.send("ChipUpdate:-1");
                } else {
                    chips += chipAmount;
                    balance -= (double) chipAmount * 100;
                    exchangeInput = true;
                    System.out.println("Client " + client_ID + " hat " + chipAmount + " Coins erworben!");
                    sendChipCount(chips);
                }
            }
            else if (message.startsWith("bet:")) {
                askInput = message.substring("bet:".length()).trim();
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


    public void handleQuit(){ // Bearbeitung eines gewollten oder ungewollten Beenden des Programms
        if(running){
            updateBalance(chips);
            this.currentThread.interrupt();
        }
        if(conn != null){
            conn.close();
        }
    }
    //endregion
}