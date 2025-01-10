// Klasse für Spielkarten
public class GameCard {
    private char value; // "Wert" der Karte in Blackjack
    private char coat; // "Farbe" der Karte

    // Konstruktor
    public GameCard(char value, char coat){
        this.setValue(value);
        this.setCoat(coat);
    }

    //region Getter und Setter
    public char getValue() {
        return value;
    }

    public void setValue(char valueOfCard) {
        this.value = valueOfCard;
    }

    public char getCoat() {
        return coat;
    }

    public void setCoat(char coat) {
        this.coat = coat;
    }
    //endregion
}