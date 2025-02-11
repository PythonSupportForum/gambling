//Damit mach auch über Java eine Klasse hat um User in der Datnebnk verwalten zu können, weild as ja eig. die Aufgabe war.

import java.sql.*;
import java.util.Scanner;

public class User {
    private int id;
    private String name;
    private String vorname;
    private String benutzername;
    private String passwortHash;
    private String passwortSalt;
    private String geburtsdatum;
    private int adresseId;

    // Konstruktor
    public User(int id, String name, String vorname, String benutzername, String passwortHash, String passwortSalt, String geburtsdatum, int adresseId) {
        //An den Construktur werden alle Daten des Benuzuers die in der Tabelle Kunden in der Datenbank sind übergebem
        this.id = id;
        this.name = name;
        this.vorname = vorname;
        this.benutzername = benutzername;
        this.passwortHash = passwortHash;
        this.passwortSalt = passwortSalt;
        this.geburtsdatum = geburtsdatum;
        this.adresseId = adresseId;
    }

    // Methode zum Starten des Scanners für die Benutzereingabe
    public static User starteUserErstellenScanner() {
        //Über einen Konsolen Scanner werden alle benötigen Infos zu einem neuen Benuzuer abgefragt!
        Scanner scanner = new Scanner(System.in);

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Vorname: ");
        String vorname = scanner.nextLine();

        System.out.print("Benutzername: ");
        String benutzername = scanner.nextLine();

        System.out.print("Passwort: ");
        String passwort = scanner.nextLine();

        System.out.print("Geburtsdatum (YYYY-MM-DD): ");
        String geburtsdatum = scanner.nextLine();

        System.out.print("Stadt: ");
        String stadt = scanner.nextLine();

        System.out.print("Postleitzahl: ");
        int postleitzahl = Integer.parseInt(scanner.nextLine());

        System.out.print("Straße: ");
        String strasse = scanner.nextLine();

        System.out.print("Hausnummer: ");
        int hausnummer = Integer.parseInt(scanner.nextLine());

        // Benutzer erstellen
        return User.create(name, vorname, benutzername, passwort, geburtsdatum, stadt, postleitzahl, strasse, hausnummer);
    }

    // Methode zum Erstellen eines Benutzers in der Datenbank
    public static User create(String name, String vorname, String benutzername, String passwort, String geburtsdatum, String stadt, int postleitzahl, String strasse, int hausnummer) {
        //Es werden die Tabellen in der Datenbank zu einem Benutzer angeelegt. Stadt und Adresse werden in seperate Tabeellen ausgelegt, da diese auch redundant sein können. Dazu wird immer erst geprüft ob eine identische Stadt bereits exestiert und nur als ID referenz in der Kunden Tabelle gespeicher

        Connection conn = null; //Vorher definieren und im try catch initaliseren um danach darauf zugreifen zu können
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            //Ich weiß eigendlich könnte man die Datenbankverbindung auch einmalig global erstellen, aber dadurch würde eine dauerhafte Verbindung Leistung beanspruchen und für Instabilität sorgen, da das Objet unbrauchbar wird, wenn das Internet Kurs weg ist.
            conn = DriverManager.getConnection("jdbc:mysql://db.ontubs.de/gambling", "carl", "geilo123!");

            // Stadt in die City-Tabelle einfügen, falls sie noch nicht existiert
            int cityId;
            stmt = conn.prepareStatement("SELECT id FROM City WHERE name = ? AND postcode = ?");
            stmt.setString(1, stadt);
            stmt.setInt(2, postleitzahl);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                stmt = conn.prepareStatement("INSERT INTO City (name, postcode) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, stadt);
                stmt.setInt(2, postleitzahl);
                stmt.executeUpdate();
                rs = stmt.getGeneratedKeys();
                rs.next();
                cityId = rs.getInt(1);
            } else {
                cityId = rs.getInt("id");
            }

            // Adresse in die Adressen-Tabelle einfügen, falls sie noch nicht existiert
            int adresseId;
            stmt = conn.prepareStatement("SELECT id FROM Adressen WHERE straße = ? AND number = ? AND cityId = ?");
            stmt.setString(1, strasse);
            stmt.setInt(2, hausnummer);
            stmt.setInt(3, cityId);
            rs = stmt.executeQuery();

            if (!rs.next()) {
                stmt = conn.prepareStatement("INSERT INTO Adressen (straße, number, cityId) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, strasse);
                stmt.setInt(2, hausnummer);
                stmt.setInt(3, cityId);
                stmt.executeUpdate();
                rs = stmt.getGeneratedKeys();
                rs.next();
                adresseId = rs.getInt(1);
            } else {
                adresseId = rs.getInt("id");
            }

            // Passwort hashen und Salt generieren
            String salt = generateSalt();
            String passwortHash = hashPassword(passwort, salt);

            // Benutzer in die Kunden-Tabelle einfügen
            stmt = conn.prepareStatement("INSERT INTO Kunden (Name, Vorname, bn, pwdhash, pwdsalt, Geburtsdatum, adresseId) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, name);
            stmt.setString(2, vorname);
            stmt.setString(3, benutzername);
            stmt.setString(4, passwortHash);
            stmt.setString(5, salt);
            stmt.setString(6, geburtsdatum);
            stmt.setInt(7, adresseId);
            stmt.executeUpdate();

            // Kunden-id abrufen
            rs = stmt.getGeneratedKeys();
            rs.next();
            int userId = rs.getInt(1);

            // Userobjekt zurückgeben
            return new User(userId, name, vorname, benutzername, passwortHash, salt, geburtsdatum, adresseId);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            // Anfrage Schließen und Verbindung löschen => Spart Resourcen
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static String generateSalt() {
        // Soll einen Zufälligen String erstellen in Hexadezimal zeichen um Passwörter sicherer zu machen
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    // Methode zum Hashen des Passworts
    private static String hashPassword(String passwort, String salt) {
        // Ganz Primitiver Hash (Achtung nicht Sicher!!!) Aber für Demonstartionen bei unserer Präsentation ausreichend
        return Integer.toHexString((passwort + salt).hashCode());
    }


    //Bli Bla Blub Getter und Setter weil wir das so gelernt haben
    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getVorname() {
        return vorname;
    }
    public String getBenutzername() {
        return benutzername;
    }
    public String getPasswortHash() {
        return passwortHash;
    }
    public String getPasswortSalt() {
        return passwortSalt;
    }
    public String getGeburtsdatum() {
        return geburtsdatum;
    }
    public int getAdresseId() {
        return adresseId;
    }
    // Main-Methode zum Testen
    public static void main(String[] args) {
        //Zum Testen des Benutzerstellens eine Main Methode wenn keine Anderen Klassen da

        User user = User.starteUserErstellenScanner();
        if (user != null) System.out.println("Benutzer erfolgreich erstellt mit der kunden id: " + user.getId());
        else System.out.println("Fehler beim erstellen des Benutzers.");
    }
}