package shared_objects;

import java.io.Serializable;
import java.util.*;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gameId;
    private int playerCount;
    private int spectaterCount;
    private Theme theme;
    private int turnCount;
    private String[][] cardMatrix;
    private int hostId;
    private String[] playorder;
    private String[] scores;
    private String status;
    private Move lastMove;
    private int currentplayer;

    //opbouwen kaartmatrix****************************************************************************
    private ArrayList<Integer> themeArray;
    private int[] cardAmountList;
    private int cardAmount;

    //Score berekening********************************************************************************
    private ArrayList<String> losers;
    private ArrayList<String> winners;

    public HashMap<String, String> getEndResult() {
        return endResult;
    }

    private HashMap<String, String> endResult;

    public void calculateEndResults() {
        losers = new ArrayList<>();
        winners = new ArrayList<>();
        int max = -1;
        for (int i = 0; i < playorder.length; i++) {
            if (Integer.parseInt(scores[i]) > max) {
                if (!winners.isEmpty()) {
                    losers.addAll(winners);
                    winners = new ArrayList<>();
                }
                winners.add(playorder[i]);
                max = Integer.parseInt(scores[i]);
            } else if (Integer.parseInt(scores[i]) == max) {
                winners.add(playorder[i]);
            } else {
                losers.add(playorder[i]);
            }
        }
    }

    public void addSpectater(){
        spectaterCount++;
    }

    @Override
    public boolean equals(Object o) {

        Game game = (Game) o;
        /*System.out.println(playerCount == game.playerCount);
        System.out.println(spectaterCount == game.spectaterCount);
        System.out.println(turnCount == game.turnCount);
        System.out.println(hostId == game.hostId);
        System.out.println(currentplayer == game.currentplayer);

        System.out.println(gameId.equals(game.gameId));

        System.out.println(cardMatrixToString().equals(game.cardMatrixToString()));
        System.out.println(playOrderToString().equals(game.playOrderToString()));
        System.out.println(scoresToString().equals(game.scoresToString()));
        System.out.println(status.equals(game.status));*/
        if (lastMove != null) {
            // System.out.println(lastMove.equals(game.lastMove));
            return playerCount == game.playerCount &&
                    spectaterCount == game.spectaterCount &&
                    turnCount == game.turnCount &&
                    hostId == game.hostId &&
                    currentplayer == game.currentplayer &&
                    gameId.equals(game.gameId) &&
                    //Objects.equals(theme, game.theme) &&
                    cardMatrixToString().equals(game.cardMatrixToString()) &&
                    playOrderToString().equals(game.playOrderToString()) &&
                    scoresToString().equals(game.scoresToString()) &&
                    status.equals(game.status) &&
                    lastMove.equals(game.lastMove);
        } else {
            return game.lastMove == null;
        }


    }


    public Game(String gameId, int playerCount, int currentplayer) {
        this.currentplayer = currentplayer;
        this.playerCount = playerCount;
        this.gameId = gameId;
    }

    public Game(int playerCount, int gridSize, String login, Theme theme) {
        losers = new ArrayList<>();
        winners = new ArrayList<>();
        endResult = new HashMap<>();
        this.playerCount = playerCount;
        turnCount = 0;
        spectaterCount = 0;
        if (playerCount == 1) {
            status = "started";
        } else {
            status = "pending";
        }

        this.theme = theme;
        cardMatrix = new String[gridSize][gridSize];
        playorder = new String[playerCount];
        playorder[0] = login;
        scores = new String[playerCount];
        for (int i = 0; i < playerCount; i++) {
            scores[i] = "0";
        }
        themeArray = new ArrayList<Integer>();
        currentplayer = 0;
        generateThemeSet();
        generateMatrix();


    }

    public Theme getTheme() {
        return theme;
    }


    private void generateMatrix() {
        for (int i = 0; i < cardMatrix.length; i++) {
            for (int j = 0; j < cardMatrix[i].length; j++) {
                cardMatrix[i][j] = randomCard();
                if (cardMatrix[i][j].length() == 3) {
                    System.out.print(cardMatrix[i][j] + " ");
                } else {
                    System.out.print(cardMatrix[i][j] + "  ");
                }

            }
            System.out.println();
        }


    }

    public void increasePlayer() {
        currentplayer = (currentplayer + 1) % playerCount;
    }

    private String randomCard() {
        Random rand = new Random();
        boolean cardGet = true;
        int index;
        int cardId;
        while (cardGet) {
            index = rand.nextInt(themeArray.size());
            cardId = themeArray.get(index);

            if (cardAmountList[cardId - 1] > 0) {
                //cardAmount -= 1;
                cardAmountList[cardId - 1] -= 1;
                if (cardAmountList[cardId - 1] == 0) {
                    themeArray.remove(index);
                    return "0" + cardId;
                }
                return "0" + cardId;
            }

        }

        return "";
    }

    public int userExists(String login) {
        for (int i = 0; i < getPlayorder().length; i++) {
            if (playorder[i].equals(login)) {
                return i;
            }
        }
        return -1;
    }

    public void removeUser(String login) {
        int userIndex = userExists(login);
        if (userIndex != -1) {

            playerCount--;

            if (playerCount != 0) {
                String[] tempPlayorder = new String[playerCount];
                String[] tempscores = new String[playerCount];
                int j = 0;
                for (int i = 0; i < playorder.length; i++) {

                    if (userIndex != i) {
                        tempPlayorder[j] = playorder[i];
                        tempscores[j] = scores[i];
                        j++;
                    }
                }

                playorder = tempPlayorder;
                scores = tempscores;
                if (currentplayer == userIndex) {
                    if (currentplayer + 1 == playerCount + 1) {
                        currentplayer = (currentplayer + 1) % (playerCount);
                    }
                }
            } else {
                status = "finished";
            }
        }
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
        cardAmount = themeSet.size() * 2;
        themeArray.addAll(themeSet);
    }

    public String cardMatrixToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(cardMatrix.length);
        sb.append("x");
        sb.append(cardMatrix[0].length);
        for (int i = 0; i < cardMatrix.length; i++) {
            for (int j = 0; j < cardMatrix[i].length; j++) {
                sb.append("-");
                sb.append(cardMatrix[i][j]);

            }
        }
        return sb.toString();

    }

    public void readCardMatrixFromString(String cardMatrixString) {
        String[] dimAndDat = cardMatrixString.split("-");
        String[] dim = dimAndDat[0].split("x");
        int rows = Integer.parseInt(dim[0]);
        int columns = Integer.parseInt(dim[1]);
        int k = 1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                cardMatrix[i][j] = dimAndDat[k];
                k++;
            }
        }
    }

    public String playOrderToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < playorder.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            sb.append(playorder[i]);
        }
        return sb.toString();
    }

    public void setPlayOrderFromString(String playOrderString) {
        playorder = playOrderString.split("-");
    }

    public String scoresToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < scores.length; i++) {
            if (i != 0) {
                sb.append("-");
            }
            sb.append(scores[i]);
        }
        return sb.toString();
    }

    public Game(Game game) {
        this.gameId = game.getGameId();
        this.playerCount = game.getPlayerCount();
        this.spectaterCount = game.getSpectaterCount();
        this.theme = game.getTheme();
        this.turnCount = game.getTurnCount();
        this.cardMatrix = game.getCardMatrix();
        this.hostId = game.getHostId();
        this.playorder = game.getPlayorder();
        this.scores = game.getScores();
        this.status = game.getStatus();
        this.lastMove = game.getLastMove();
        this.currentplayer = game.getCurrentplayer();
    }

    public void increaseScore() {
        int score = Integer.parseInt(scores[currentplayer]);
        score++;
        scores[currentplayer] = String.valueOf(score);
    }

    public void setScoresFromString(String scoresString) {
        scores = scoresString.split("-");
    }

    public void increaseTurnCount() {
        turnCount++;
    }


    public String[][] getCardMatrix() {
        return cardMatrix;
    }

    public String getGameId() {
        return gameId;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getSpectaterCount() {
        return spectaterCount;
    }

    public int getTurnCount() {
        return turnCount;
    }

    public int getHostId() {
        return hostId;
    }

    public String[] getPlayorder() {
        return playorder;
    }

    public String[] getScores() {
        return scores;
    }

    public String getStatus() {
        return status;
    }

    public void setGameId(String spelid) {
        gameId = spelid;
    }

    public void setLastMove(Move lastMove) {
        this.lastMove = lastMove;
    }

    public int getCurrentplayer() {
        return currentplayer;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean checkFinished() {

        for (int i = 0; i < cardMatrix.length; i++) {
            for (int j = 0; j < cardMatrix[i].length; j++) {
                if (cardMatrix[i][j].charAt(0) != '1') {
                    return false;
                }
            }
        }
        return true;
    }

    public void addPlayer(String login) {
        currentplayer++;
        System.out.println("inside addplayer currentplayer: " + currentplayer);
        playorder[currentplayer] = login;
        if (currentplayer + 1 == playerCount) {
            status = "started";
        }

    }

    public Move getLastMove() {
        return lastMove;
    }

    public ArrayList<String> getLosers() {
        return losers;
    }

    public ArrayList<String> getWinners() {
        return winners;
    }
}
