package Admin;

public class Main {
    public static AdminPanel adminPanel = new AdminPanel();

    public static void main(String[] args) {
        while(true) {
            adminPanel.update();
        }
    }

}

