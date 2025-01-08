import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.Scanner;

public class GameThread implements Runnable{

    int client_ID;

    public enum GameState
    {
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

    GameState gameState = GameState.IDLE;

    //Konstruktor
    public GameThread(int id){
        client_ID = id;
    }

    public void run(){
        game(client_ID);
    }

    // Funktioniert als Hauptmethode für das Blackjack Spiel
    public void game(int client_ID){
        //Start des Spiels
        List<GameCard> dealerStack = new ArrayList<>();
        List<GameCard> playerStack = new ArrayList<>();
        Stack<GameCard> deck = new Stack<>();
        setGameState(GameState.DEPOSIT);
        setGameState(GameState.START);
        setGameState(GameState.BET);
        setGameState(GameState.SHUFFLE);
        List<GameCard> a = new ArrayList<GameCard>();

        //region Karten hinzufügen
        // Clubs (Kreuz)
        a.add(new GameCard('2', 'c'));
        a.add(new GameCard('3', 'c'));
        a.add(new GameCard('4', 'c'));
        a.add(new GameCard('5', 'c'));
        a.add(new GameCard('6', 'c'));
        a.add(new GameCard('7', 'c'));
        a.add(new GameCard('8', 'c'));
        a.add(new GameCard('9', 'c'));
        a.add(new GameCard('0', 'c')); // 10
        a.add(new GameCard('j', 'c')); // Bube
        a.add(new GameCard('q', 'c')); // Dame
        a.add(new GameCard('k', 'c')); // König
        a.add(new GameCard('a', 'c')); // Ass

        // Diamonds (Karo)
        a.add(new GameCard('2', 'd'));
        a.add(new GameCard('3', 'd'));
        a.add(new GameCard('4', 'd'));
        a.add(new GameCard('5', 'd'));
        a.add(new GameCard('6', 'd'));
        a.add(new GameCard('7', 'd'));
        a.add(new GameCard('8', 'd'));
        a.add(new GameCard('9', 'd'));
        a.add(new GameCard('0', 'd')); // 10
        a.add(new GameCard('j', 'd')); // Bube
        a.add(new GameCard('q', 'd')); // Dame
        a.add(new GameCard('k', 'd')); // König
        a.add(new GameCard('a', 'd')); // Ass

        // Hearts (Herz)
        a.add(new GameCard('2', 'h'));
        a.add(new GameCard('3', 'h'));
        a.add(new GameCard('4', 'h'));
        a.add(new GameCard('5', 'h'));
        a.add(new GameCard('6', 'h'));
        a.add(new GameCard('7', 'h'));
        a.add(new GameCard('8', 'h'));
        a.add(new GameCard('9', 'h'));
        a.add(new GameCard('0', 'h')); // 10
        a.add(new GameCard('j', 'h')); // Bube
        a.add(new GameCard('q', 'h')); // Dame
        a.add(new GameCard('k', 'h')); // König
        a.add(new GameCard('a', 'h')); // Ass

        // Spades (Pik)
        a.add(new GameCard('2', 's'));
        a.add(new GameCard('3', 's'));
        a.add(new GameCard('4', 's'));
        a.add(new GameCard('5', 's'));
        a.add(new GameCard('6', 's'));
        a.add(new GameCard('7', 's'));
        a.add(new GameCard('8', 's'));
        a.add(new GameCard('9', 's'));
        a.add(new GameCard('0', 's')); // 10
        a.add(new GameCard('j', 's')); // Bube
        a.add(new GameCard('q', 's')); // Dame
        a.add(new GameCard('k', 's')); // König
        a.add(new GameCard('a', 's')); // Ass
        // endregion

        while(!a.isEmpty()){
            Random b = new Random();
            int random = b.nextInt(a.size());
            deck.push(a.get(random));
            a.remove(random);
        }

        setGameState(GameState.DEALER_START);

        dealerStack.add(deck.pop());
        dealerStack.add(deck.pop());
        if(dealerStack.get(1).getCoat() == 'a'){
            //ask for Insurance Bet
        }
        
        setGameState(GameState.PLAYER_DRAW);
        boolean input = false;
        while(!input){
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
}


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
//endregion
