package shared_objects;

import java.util.HashMap;
import java.util.Map;

public class Theme {

    int size;
    HashMap<String,String> cardMap;

    public int getSize() {
        return size;
    }

    public Theme(){
        size = 32;
        cardMap = new HashMap<>();
        setCards();
    }

    public void setCards(){
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                cardMap.put(String.valueOf(i),"memory_resources/pokemon/pokemon"+"_backside.jpg");
            }
            cardMap.put(String.valueOf(i+1), "memory_resources/pokemon/pokemon" + (i+1) + ".jpg");
        }
        for (Map.Entry<String, String> entry : cardMap.entrySet()) {
            System.out.println("key: " + entry.getKey() + " path: "+entry.getValue());
        }
    }

    public String getImage(String id) {
        return cardMap.get(id);
    }
}
