import java.util.Scanner;

public class Main {



    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        startSession(0);
    }

    public static void startSession(int ID){
        Thread thread = new Thread(new GameThread(ID));
        thread.start();
    }
}

