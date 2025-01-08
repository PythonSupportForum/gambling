public class GameCard {
    private char valueOfCard;
    private char coat;

    public GameCard(char valueOfCard, char coat){
        this.setId(valueOfCard);
        this.setCoat(coat);
    }

    //region Getter und Setter
    public char getId() {
        return valueOfCard;
    }

    public void setId(char valueOfCard) {
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