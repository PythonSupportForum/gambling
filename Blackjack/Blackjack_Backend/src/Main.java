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

    }

    public void startSession(){
        Thread thread = new Thread(new GameThread(5));
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