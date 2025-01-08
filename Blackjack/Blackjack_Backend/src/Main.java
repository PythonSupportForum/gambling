import javax.smartcardio.Card;
import java.util.Scanner;

public class Main {

    public enum GameState
    {
        IDLE,
        DEPOSIT,
        START,
        BET,
        DRAW,
        DEALER_START,
        PLAYER_DRAW,
        DEALER_DRAW,
        PLAYER_WON,
        PLAYER_LOST,
        WITHDRAW
    }

    GameState gameState = GameState.IDLE;

    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        startSession(5);
    }

    public static void startSession(int ID){
        Thread thread = new Thread();
        thread.start();
    }

    public void game(int client_ID){
        setGameState(GameState.DEPOSIT);
    }

    class GameThread implements Runnable{
        int client_ID;

        public GameThread(int id){
            client_ID = id;
        }

        public void run(){
            game(client_ID);
        }
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