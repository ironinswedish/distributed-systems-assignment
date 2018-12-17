package shared_objects;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;

public class Person implements Serializable {

    SimpleIntegerProperty score;
    SimpleStringProperty name;
    SimpleIntegerProperty rank;

    public Person(int s, String n){
        score=new SimpleIntegerProperty(s);
        name=new SimpleStringProperty(n);
    }

    public Person(int s, String n, int r){
        score=new SimpleIntegerProperty(s);
        name=new SimpleStringProperty(n);
        rank=new SimpleIntegerProperty(r);
    }


    public void setScore(int score) {
        this.score = new SimpleIntegerProperty(score);
    }

    public void setName(String name) {
        this.name = new SimpleStringProperty(name);
    }

    public void setRank(int rank){ this.rank = new SimpleIntegerProperty(rank); }

    public int getScore() {
        return score.get();
    }

    public String getName() {
        return name.get();
    }

    public int getRank() { return rank.get(); }
}
