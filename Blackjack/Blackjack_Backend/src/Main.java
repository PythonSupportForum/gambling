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
        Thread thread = new Thread(new GameThread());
        thread.start();
    }

    public void game(){
        setGameState(GameState.DEPOSIT);
    }

    class GameThread implements Runnable{
        int client_ID;

        public GameThread(){

        }

        public void run(){
            game();
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