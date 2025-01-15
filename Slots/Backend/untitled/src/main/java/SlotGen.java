package Slots.Backend.untitled.src.main.java;

import java.io.File;
import java.util.Arrays;
import java.util.Random;






public class SlotGen{

    //Nutzerbezogende Daten
    public int userID;
    public int einsatz;
    public int balance;
    public boolean valid;

    //Verbindung mit Server
    Statement stmt;
    Connection conn;

    //Constructor, dem der Ordner
    public SlotGen(File folder, int id, int pEinsatz) {
        getSlotArray(File folder);
        userID = id;
        einsatz = pEinsatz;

        clientDB = getConnection();

        //Der Kontostand des Nutzers wird aufgerufen und in balance gespeichert
        String query = "SELECT Kontostand FROM Kunden WHERE id = " + userID;
        String queryTwo = "SELECT verifiziert FROM Kunden WHERE id = " + userID;
        try{
            assert clientDB != null;
            stmt = clientDB.createStatement();
            stmt.executeQuery(query);
            ResultSet rs = stmt.getResultSet();
            rs.next();
            balance = rs.getDouble("Kontostand");

            stmt.executeQuery(queryTwo);
            rs = stmt.getResultSet();
            rs.

            rs.close();
            stmt.close();
        }
        catch(SQLException e){e.printStackTrace();}

        if (einsatz <= balance) {
            valid = true;
        }
    }

    /**
     *Gibt ein Array mit den Pfaden zu drei zufälligen Bildern zurück.
     * @param folder Der Pfad zu dem Ordner mit den Bildern.
     * @return Das String-Array.
     */
    public static String[] getSlotArray(File folder) {
        
            String[] output = new String[3];
            Random rand = new Random();
            File[] files = folder.listFiles();

            for(int i = 0; i<3; i++){
                assert files != null;
                output[i] = files[rand.nextInt(files.length)].getPath();
            }

            return output;
        }
    }

    /**
     *Gibt ein Array mit den Pfaden zu drei zufälligen Bildern zurück.
     * @return Das String-Array.
     */
    public static String[] getSlotArray() {

        folder = new File("slots_icons");
        String[] output = new String[3];
        Random rand = new Random();
        File[] files = folder.listFiles();

        for(int i = 0; i<3; i++){
            assert files != null;
            output[i] = files[rand.nextInt(files.length)].getPath();
        }

        return output;
    }

    public Connection getConnection(){
        String addr = "jdbc:mariadb:"
    }

    public Connection getConnection() {
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
}