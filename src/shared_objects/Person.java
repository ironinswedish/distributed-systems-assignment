package shared_objects;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Person {

    SimpleIntegerProperty score;
    SimpleStringProperty name;

    public Person(int s, String n){
        score=new SimpleIntegerProperty(s);
        name=new SimpleStringProperty(n);
    }


    public void setScore(int score) {
        this.score = new SimpleIntegerProperty(score);
    }

    public void setName(String name) {
        this.name = new SimpleStringProperty(name);
    }

    public int getScore() {
        return score.get();
    }

    public String getName() {
        return name.get();
    }
}
