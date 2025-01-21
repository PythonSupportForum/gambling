package Slots.Backend.untitled.src.main.java;

import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.Random;


public class SlotGen {

    //Nutzerbezogende Daten
    public int userID;
    public int einsatz;
    public double balance;
    public boolean valid;

    //Verbindung mit Server
    Statement stmt;
    Connection conn;

    //Constructor, dem der Ordner der Bilddateien, die ID des Spielenden und sein Einsatz übermittelt wird
    public SlotGen(File folder, int id, int pEinsatz) {
        getSlotArray(folder);
        userID = id;
        einsatz = pEinsatz;

        Connection clientDB = getConnection();

        //Der Kontostand und die Verifizierung des Nutzers wird aufgerufen und in balance, valid gespeichert
        String query = "SELECT * FROM Kunden WHERE id = " + userID;
        try{
            assert clientDB != null;
            stmt = clientDB.createStatement();
            stmt.executeQuery(query);
            ResultSet rs = stmt.getResultSet();
            rs.next();
            balance = rs.getDouble("Kontostand");
            valid = rs.getBoolean("verifiziert");
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


    /**
     *Gibt ein Array mit den Pfaden zu drei zufälligen Bildern zurück.
     * @return Das String-Array.
     */
    public static String[] getSlotArray() {

        File folder = new File("slots_icons");
        String[] output = new String[3];
        Random rand = new Random();
        File[] files = folder.listFiles();

        for(int i = 0; i<3; i++){
            assert files != null;
            output[i] = files[rand.nextInt(files.length)].getPath();
        }

        return output;
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