package shared_objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Game {
    private int gameId;
    private int playerCount;
    private int spectaterCount;
    private Theme theme;
    private int turnCount;
    private String[][] cardMatrix;
    private int hostId;
    private int[] playorder;
    private int[] scores;
    private String status;
    private ArrayList<Integer> themeArray;
    private int[] cardAmountList;
    private int cardAmount;

    public Game(int playerCount, int gridSize, Theme theme) {
        this.playerCount = playerCount;
        turnCount = 0;
        spectaterCount = 0;
        status = "pending";
        this.theme = theme;
        cardMatrix = new String[gridSize][gridSize];
        playorder = new int[playerCount];
        scores = new int[playerCount];
        themeArray = new ArrayList<Integer>();
        generateThemeSet();
        generateMatrix();

    }

    public Theme getTheme() {
        return theme;
    }


    private void generateMatrix(){
        for (int i = 0; i < cardMatrix.length; i++) {
            for (int j = 0; j < cardMatrix[i].length; j++) {
                cardMatrix[i][j] = randomCard();
                if (cardMatrix[i][j].length() == 3) {
                    System.out.print(cardMatrix[i][j]+" ");
                } else{
                    System.out.print(cardMatrix[i][j]+"  ");
                }

            }
            System.out.println();
        }


    }

    private String randomCard() {
        Random rand = new Random();
        boolean cardGet = true;
        int index;
        int cardId;
        while (cardGet) {
            index = rand.nextInt(themeArray.size());
            cardId = themeArray.get(index);

            if (cardAmountList[cardId-1] > 0) {
                //cardAmount -= 1;
                cardAmountList[cardId-1] -= 1;
                if (cardAmountList[cardId - 1] == 0) {
                    themeArray.remove(index);
                    return "0" + cardId;
                }
                return "0" + cardId;
            }

        }

        return "";
    }

    private void generateThemeSet() {
        Random rand = new Random();
        int themeSize = theme.getSize();
        Set<Integer> themeSet = new HashSet<Integer>();
        int setSize = cardMatrix.length * cardMatrix.length / 2;
        while (themeSet.size() < setSize) {
            themeSet.add(rand.nextInt(themeSize) + 1);
        }
        cardAmountList = new int[themeSize];
        for (int i = 0; i < themeSize; i++) {
            cardAmountList[i] = 2;
        }
        cardAmount = themeSet.size()*2;
        themeArray.addAll(themeSet);
    }

    public String[][] getCardMatrix(){
        return cardMatrix;
    }
}
