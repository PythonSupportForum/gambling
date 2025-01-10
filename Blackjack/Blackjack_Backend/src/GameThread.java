import org.java_websocket.WebSocket;

import java.util.*;

// Implementiert das Runnable interface -> Nutzung der Java Implementation für Multithreading
public class GameThread implements Runnable {

    boolean running = true;
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
        INSURANCE_BET,
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
        System.out.print(client_ID + "\n");
        game(client_ID); // ruft Hauptmethode des Spiels auf, beginnt Spiel mit dem Client
    }

    // Verarbeiten einer einkommenden Nachricht vom Client
    public void handleMessage(String message) {
        switch (message.toLowerCase()) {
            case "exchange":
                    break;
            case "accept":
                break;
            case "bet":
                break;
            case "insurance":
                break;
            case "end":
                running = false;
                break;
            default:
                System.out.println("Client Message: " + message);
                break;
        }
    }

    // Funktioniert als Hauptmethode für das Blackjack Spiel
    public void game(int client_ID) {
        //Start des Spiels
        int coins = 0;
        double balance = 0.0;
        //Den Kontostand abfragen in der Datenbank
        int bet = 0;
        int splitBet = 0;
        int insuranceBet = 0;

        // unveränderte Liste aller Karten
        List<GameCard> availableCards = new ArrayList<GameCard>();
        // Liste, aus welcher die Karten entnommen werden, welche dann in den Stacks landen
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
        List<GameCard> playerSplitStack = new ArrayList<>();
        Stack<GameCard> deck = new Stack<>();

        while (running) {
            setGameState(GameState.DEPOSIT);

            // füllt temp mit allen Karten
            temp.addAll(availableCards);

            //region Geld umtauschen
            boolean input = false;
            int coinAmount = 0;

            while (!input) {
                //ist nich vollständig, nach mit Frontend lösen
                Scanner c = new Scanner(System.in);
                String inputString = c.nextLine();
                try {
                    coinAmount = Integer.parseInt(inputString);
                    input = true;
                } catch (NumberFormatException e) {
                    continue;
                }

            }

            if (balance - (coinAmount * 100) < 0) {
                System.out.println("Du bist broke du Bastard!");
            } else {
                coins += coinAmount;
                balance -= coinAmount * 100;
                System.out.println("Du hast " + coinAmount + " Coins umgetauscht!");
            }
            //endregion
            setGameState(GameState.START);

            setGameState(GameState.BET);
            //region Geld setzen
            boolean input2 = false;
            while (!input2) {
                //ist nich vollständig, nach mit Frontend lösen
                Scanner c = new Scanner(System.in);
                String inputString = c.nextLine();
                try {
                    bet = Integer.parseInt(inputString);
                    if (bet > coins) {
                        System.out.println("Du hast nicht genug Coins um zu spielen!");
                        bet = 0;
                    } else {
                        coins -= bet;
                        System.out.println("Du hast " + bet + " Coins gesetzt!");
                        input2 = true;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }

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
            if (dealerStack.get(1).getValueOfCard() == '0' || dealerStack.get(1).getValueOfCard() == 'j' || dealerStack.get(1).getValueOfCard() == 'q' || dealerStack.get(1).getValueOfCard() == 'k') {
                //region Insurance Bet
                setGameState(GameState.INSURANCE_BET);
                boolean input4 = false;
                while (!input4) {
                    //ist nicht vollständig, nachher mit Frontend lösen
                    Scanner c = new Scanner(System.in);
                    String inputString = c.nextLine();
                    try {
                        insuranceBet = Integer.parseInt(inputString);
                        input4 = true;
                    } catch (NumberFormatException e) {}
                }
                //endregion
            }

            setGameState(GameState.PLAYER_DRAW);
            playerStack.add(deck.pop());
            playerStack.add(deck.pop());
            boolean input3 = false;
            while (!input3) {
                //ist nicht vollständig, nachher mit Frontend lösen
                if (currentValue(playerStack) > 21) {
                    setGameState(GameState.PLAYER_LOST);
                    System.out.println("Bust! Du hast verloren!");
                    break;
                }
                if (currentValue(playerStack) == 21) {
                    System.out.println("Herzlichen Glückwunsch! Du hast einen Blackjack!");
                    break;
                }
                Scanner c = new Scanner(System.in);
                String inputString = c.nextLine();
                try {
                    if (Boolean.parseBoolean(inputString)) {
                        playerStack.add(deck.pop());
                        continue;
                    } else {
                        input3 = true;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }

            }

            setGameState(GameState.DEALER_DRAW);

            int total = 0;
            int aceCounter = 0;
            if (dealerStack.get(0).getValueOfCard() == 'a') {
                aceCounter += 1;
            }
            if (dealerStack.get(1).getValueOfCard() == 'a') {
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
                if (aceCounter > 0 && total > 21) {
                    total -= 10;
                    aceCounter -= 1; // Ass Counter einen herabsetzen
                }
            }

            if (currentValue(dealerStack) < currentValue(playerStack)) {
                coins += (bet * 2);
                System.out.println("Du hast gewonnen!");
                // Unser Kontostand muss um den Wert der Coins verringert werden, also Anzahl der Coins * 100
                setGameState(GameState.PLAYER_WON);
            } else if (currentValue(dealerStack) == currentValue(playerStack)) {
                coins += bet;
                System.out.println("Unentschieden!");
                setGameState(GameState.WITHDRAW);
            } else if (currentValue(dealerStack) > currentValue(playerStack)) {
                System.out.println("Du hast verloren!");
                // Unser Kontostand muss um den Wert der Coins erhöht werden, also Anzahl der Coins * 100
                setGameState(GameState.PLAYER_LOST);
            } else {
                System.out.println("Ups??!? Ein Fehler ist aufgetreten!");
            }
            if (insuranceBet > 0 && dealerStack.get(0).getValueOfCard() == 'a') {
                coins += insuranceBet;
                System.out.println("Du hast den Insurance Bet erhalten!");
            }
            bet = 0;
            insuranceBet = 0;
        }

            conn.close(); // Verbindung beenden
            currentThread.interrupt();// Beende den Thread
    }

        //region currentValue-Methode
        public int currentValue (List<GameCard> playerStack) {
            int totalValue = 0;
            int aceCount = 0;

            for (GameCard card : playerStack) {
                char valueOfCard = card.getCoat();

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
        //endregion

        //region Getter and Setter
        public GameState getGameState () {
            return gameState;
        }

        public void setGameState (GameState gameState){
            this.gameState = gameState;
        }
        //endregion


    }

