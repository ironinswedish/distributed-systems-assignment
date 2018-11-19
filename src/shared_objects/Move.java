package shared_objects;

import java.io.Serializable;
import java.util.Objects;

public class Move implements Serializable {

    private int row1;
    private int row2;
    private int column1;
    private int column2;
    private int cardid1;
    private int cardid2;
    private String user;

    @Override
    public boolean equals(Object o) {
        if (this != o) return false;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return row1 == move.row1 &&
                row2 == move.row2 &&
                column1 == move.column1 &&
                column2 == move.column2 &&
                cardid1 == move.cardid1 &&
                cardid2 == move.cardid2 &&
                user.equals( move.user);
    }

    @Override
    public int hashCode() {

        return Objects.hash(row1, row2, column1, column2, cardid1, cardid2, user);
    }



    public Move(int row1, int row2, int column1, int column2, int cardid1, int cardid2, String user) {
        this.row1 = row1;
        this.row2 = row2;
        this.column1 = column1;
        this.column2 = column2;
        this.cardid1 = cardid1;
        this.cardid2 = cardid2;
        this.user = user;
    }

    public int getRow1() {
        return row1;
    }

    public void setRow1(int row1) {
        this.row1 = row1;
    }

    public int getRow2() {
        return row2;
    }

    public void setRow2(int row2) {
        this.row2 = row2;
    }

    public int getColumn1() {
        return column1;
    }

    public void setColumn1(int column1) {
        this.column1 = column1;
    }

    public int getColumn2() {
        return column2;
    }

    public void setColumn2(int column2) {
        this.column2 = column2;
    }

    public int getCardid1() {
        return cardid1;
    }

    public void setCardid1(int cardid1) {
        this.cardid1 = cardid1;
    }

    public int getCardid2() {
        return cardid2;
    }

    public void setCardid2(int cardid2) {
        this.cardid2 = cardid2;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
