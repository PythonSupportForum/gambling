public class GameCard {
    private char id;
    private char coat;

    public GameCard(char id, char coat){
        this.setId(id);
        this.setCoat(coat);
    }

    //region Getter und Setter
    public char getId() {
        return id;
    }

    public void setId(char id) {
        this.id = id;
    }

    public char getCoat() {
        return coat;
    }

    public void setCoat(char coat) {
        this.coat = coat;
    }
    //endregion
}