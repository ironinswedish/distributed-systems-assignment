package shared_objects;

import Interfaces.ApplicationProtocol;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Theme implements Serializable {
    private static final long serialVersionUID = 1L;

    int themeId;
    int size;
    HashMap<String,byte[]> cardMap;
    String name;

    public int getSize() {
        return size;
    }

    public Theme(int themeId, HashMap<String, byte[]> cards) {
        this.themeId = themeId;
        this.cardMap = cards;
    }

    public Theme(String themeName, int size, HashMap<String,byte[]> cards){

        name=themeName;
        this.size = size;
        cardMap = cards;

        System.out.println(cardMap.size()+ " zoveel kaarten zitten er in de theme met size: "+size);

    }
/*
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
    }*/

    public InputStream getImage(String id) {

        System.out.println(this.cardMap.size()+ " is grootte van cardmap");


        return new ByteArrayInputStream(cardMap.get(id));
    }

    public HashMap<String,byte[]> getCardMap(){
        return cardMap;
    }


    public int getThemeId() {
        return themeId;
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

}
