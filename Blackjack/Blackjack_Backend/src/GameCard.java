// Klasse für Spielkarten
public class GameCard {
    private char valueOfCard; // "Wert" der Karte in Blackjack
    private char coat; // "Farbe" der Karte

    // Konstruktor
    public GameCard(char valueOfCard, char coat){
        this.setValueOfCard(valueOfCard);
        this.setCoat(coat);
    }

    //region Getter und Setter
    public char getValueOfCard() {
        return valueOfCard;
    }

    public void setValueOfCard(char valueOfCard) {
        this.valueOfCard = valueOfCard;
    }

    public char getCoat() {
        return coat;
    }

    public void setCoat(char coat) {
        this.coat = coat;
    }
    //endregion
}