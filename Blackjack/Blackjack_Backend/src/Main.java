import javax.smartcardio.Card;
import java.util.Scanner;

public class Main {



    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        startSession(5);
    }

    public static void startSession(int ID){
        Thread thread = new Thread(new GameThread(5));
        thread.start();
    }
}

