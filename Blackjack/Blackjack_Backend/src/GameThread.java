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
        setGameState(GameState.DEPOSIT);
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