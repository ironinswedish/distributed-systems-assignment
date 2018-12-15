package client;

import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.util.Duration;
import shared_objects.Game;
import shared_objects.Move;
import shared_objects.Theme;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;


import static java.lang.Thread.sleep;


public class GameController extends Controller {


    private static Game game;
    private static Theme theme;
    private int imageSize;
    final static GameJudge gamejudge = new GameJudge();
    static int score = 0;



    @FXML
    private GridPane gridPane;
    @FXML
    private Label player1;
    @FXML
    private Label player2;
    @FXML
    private Label player3;
    @FXML
    private Label player4;

    @FXML
    private Label turnCounter;

    @FXML
    private Button quitButton;


    public void setCards() throws IOException {

        System.out.println("size van het thema: " + theme.getSize());

        if(theme==null || theme.getThemeId()!=game.getTheme()){
            //Vraag thema op als dit niet gecacht was op de client
            theme = application.getTheme(game.getTheme());
        }


        String matrixSlot;
        imageSize = 85;

        for (int i = 0; i < game.getCardMatrix().length; i++) {

            for (int j = 0; j < game.getCardMatrix()[i].length; j++) {
                matrixSlot = game.getCardMatrix()[j][i];
                final String id = matrixSlot.substring(1);
                final ImageView card;
                System.out.println(id);
                if (matrixSlot.charAt(0) == '0') {
                    card = new ImageView(new Image(theme.getImage("0")));
                    card.setUserData("0");
                    card.setOnMouseClicked(event -> {

                        System.out.println("clicked image");
                        if (gamejudge.turn) {


                            if (gamejudge.counter > 0) {
                                if (card != gamejudge.card1 && gamejudge.cardSelect(card)) {
                                    card.setImage(new Image(theme.getImage(id)));
                                    if (gamejudge.counter == 2) {
                                        card.setUserData("1");
                                        gamejudge.cardid1 = Integer.parseInt(id);
                                        gamejudge.card1 = card;
                                        gamejudge.row1 = GridPane.getRowIndex(card);
                                        gamejudge.column1 = GridPane.getColumnIndex(card);
                                    } else if (gamejudge.counter == 1) {
                                        card.setUserData("1");
                                        gamejudge.cardid2 = Integer.parseInt(id);
                                        gamejudge.card2 = card;
                                        gamejudge.row2 = GridPane.getRowIndex(card);
                                        gamejudge.column2 = GridPane.getColumnIndex(card);
                                    }
                                    gamejudge.counter -= 1;
                                }
                            } else {
                                gamejudge.turn = false;
                                nextTurn();
                            }

                        }


                    });

                    //System.out.println("first char is 0");
                } else {


                    card = new ImageView(new Image(theme.getImage(id)));

                }
                card.setFitHeight(imageSize);
                card.setFitWidth(imageSize);
                gridPane.add(card, i, j);
            }

        }

        gridPane.setMinWidth(imageSize * game.getCardMatrix().length);
        gridPane.setMinHeight(imageSize * game.getCardMatrix().length);
        gridPane.setPrefSize(imageSize * game.getCardMatrix().length, imageSize * game.getCardMatrix().length);
    }

    public void setTheme(){
        System.out.println("OUDE THEME ID: "+theme.getThemeId()+ " EN NIEUWE MOET ZIJN: "+game.getTheme());
        if(game.getTheme()!=theme.getThemeId()){
            try {
                System.out.println("OUDE THEME ID: "+theme.getThemeId()+ " EN NIEUWE MOET ZIJN: "+game.getTheme());
                application.getTheme(game.getTheme());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setGame(Game game) {
        this.game = game;

        try {
        if(this.theme!=null){
            if(this.theme.getThemeId()!=game.getTheme()){

                    application.getTheme(game.getTheme());

            }
        }else{
            this.theme = application.getTheme(game.getTheme());
        }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        gamejudge.resetJudge();

        GameListener gameListener = new GameListener();
        gameListener.setOnSucceeded(event -> {
            processUpdate();
            System.out.println("game done");
            if (!this.game.getStatus().equals("finished")) {
                System.out.println("restarting gameListener");
                gameListener.reset();
                gameListener.start();
            }

        });
        gameListener.start();
    }

    public void quit() {
        try {
            application.quitGame(game, login);


            AnchorPane pane = getTransition("Lobby.fxml");

            // Stage stage = (Stage) loginKnop.getScene().getWindow();
            stage.setTitle("lobby");
            stage.setOnCloseRequest(e -> {
                try {
                    if (dispatch != null) {
                        dispatch.logout();
                    }
                    if (application != null) {
                        application.logout(login, session, true);
                    }

                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            });
            Scene scene = new Scene(pane);
            stage.setScene(scene);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void nextTurn() {

        try {
            Move move = gamejudge.getMove();
            System.out.println(move.getCardid1() + " " + move.getRow1() + " " + move.getColumn1() + " " + move.getCardid2() + " " + move.getRow2() + " " + move.getColumn2());
            Game tempgame = application.processTurn(move, login, game.getGameId());
            if (tempgame != null) {

            } else {
                System.out.println("not your turn");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void updateBoard() {
        ImageView card;
        String[][] cardMatrix = game.getCardMatrix();
        char state;
        String id;
        for (int i = 0; i < cardMatrix.length; i++) {
            for (int j = 0; j < cardMatrix[i].length; j++) {
                card = (ImageView) getNodeFromGridPane(gridPane, j, i);
                state = cardMatrix[i][j].charAt(0);
                id = cardMatrix[i][j].substring(1);
                if (!card.getUserData().equals(state)) {
                    card.setUserData(state);
                    System.out.println(state);
                    if (state == '1') {
                        System.out.println("setting new image");
                        card.setImage(new Image(theme.getImage(id)));

                    } else {

                        card.setImage(new Image(theme.getImage("0")));

                    }

                }
            }
        }
    }

    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    public void executeLastMove() {
        Move move = game.getLastMove();
        if (move != null) {

            if (!move.getUser().equals(login)) {


                ImageView card1 = (ImageView) getNodeFromGridPane(gridPane, move.getColumn1(), move.getRow1());
                System.out.println("move c1: " + move.getColumn1() + " move r1: " + move.getRow1());
                String id = game.getCardMatrix()[move.getRow1()][move.getColumn1()].substring(1);
                System.out.println("flip id" + id);
                FlipCard flip1 = new FlipCard(Duration.millis(1000), card1, id);
                //card1.setImage(new Image(new FileInputStream(theme.getImage(id))));
                flip1.setOnFinished(event -> {
                    ImageView card2 = (ImageView) getNodeFromGridPane(gridPane, move.getColumn2(), move.getRow2());
                    System.out.println("move c2: " + move.getColumn2() + " move r2: " + move.getRow2());
                    String id2 = game.getCardMatrix()[move.getRow2()][move.getColumn2()].substring(1);
                    System.out.println("flip id" + id2);
                    FlipCard flip2 = new FlipCard(Duration.millis(1000), card2, id2);
                    flip2.setOnFinished(event1 -> {
                        updateBoard();
                        Platform.runLater(new FinishWindow());


                        gamejudge.resetJudge();

                    });

                    //card2.setImage(new Image(new FileInputStream(theme.getImage(id))));
                    card2.setUserData("1");
                    flip2.play();

                    System.out.println("card 2 flipped");
                });
                card1.setUserData("1");
                flip1.play();

                System.out.println("card 1 flipped");

            } else {
                updateBoard();
                Platform.runLater(new FinishWindow());

                gamejudge.resetJudge();
            }
        }

    }

    public void setLabels() {
        turnCounter.setText("Turn: " + game.getTurnCount());
        player1.setText("");
        player1.setStyle(null);
        player2.setText("");
        player2.setStyle(null);
        player3.setText("");
        player3.setStyle(null);
        player4.setText("");
        player4.setStyle(null);
        for (int i = 0; i < game.getPlayorder().length; i++) {
            if (i == 0) {
                player1.setText(game.getPlayorder()[i] + ": " + game.getScores()[i]);
                if (i == game.getCurrentplayer()) {
                    player1.setStyle("-fx-font-weight: bold;");
                }
            } else if (i == 1) {
                player2.setText(game.getPlayorder()[i] + ": " + game.getScores()[i]);
                if (i == game.getCurrentplayer()) {
                    player2.setStyle("-fx-font-weight: bold;");
                }
            } else if (i == 2) {
                player3.setText(game.getPlayorder()[i] + ": " + game.getScores()[i]);
                if (i == game.getCurrentplayer()) {
                    player3.setStyle("-fx-font-weight: bold;");
                }
            } else if (i == 3) {
                player4.setText(game.getPlayorder()[i] + ": " + game.getScores()[i]);
                if (i == game.getCurrentplayer()) {
                    player4.setStyle("-fx-font-weight: bold;");
                }
            }
        }
    }

    public void processUpdate() {
        if (game.getPlayerCount() > 0) {


            setLabels();
            if (gamejudge.match()) {
                System.out.println("nice its a match");
                score++;
                //player1.setText("player1: " + score);

            } else {
                System.out.println("oh ow wrong");
                if (game.getLastMove() != null) {
                    if (game.getLastMove().getUser().equals(login)) {
                        gamejudge.resetChoices();
                    }
                }
            }
            executeLastMove();
        }

        //gamejudge.resetJudge();
    }


    private static class GameJudge {

        public boolean turn;
        public int counter;
        public int cardid1 = -1;
        public int cardid2 = -2;
        public ImageView card1;
        public ImageView card2;
        public int row1;
        public int row2;
        public int column1;
        public int column2;


        public GameJudge() {
            if (game != null) {
                if (game.getStatus().equals("started")) {
                    turn = true;
                    counter = 2;
                }
            } else {
                turn = false;
                counter = 2;
            }


        }

        public boolean match() {
            return cardid1 == cardid2;
        }

        public void resetJudge() {
            if (game.getStatus().equals("started")) {
                if (game.getPlayorder()[game.getCurrentplayer()].equals(login)) {
                    turn = true;
                    counter = 2;
                    cardid1 = -1;
                    cardid2 = -2;
                    card2 = null;
                    card1 = null;
                    row1 = 0;
                    row2 = 0;
                    column1 = 0;
                    column2 = 0;
                }

            }

        }

       /* public void checkStatus() {
            if (game.getStatus().equals("finished")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error message");
                alert.setHeaderText(null);
                alert.setContentText("game finished");
                alert.showAndWait();
            }
        }*/

        public void resetChoices() {
            if (card1 != null && card2 != null) {
                card1.setImage(new Image(theme.getImage("0")));
                card2.setImage(new Image(theme.getImage("0")));

            }

        }

        public boolean cardSelect(ImageView card) {
            System.out.println(GridPane.getRowIndex(card));
            System.out.println(GridPane.getColumnIndex(card));
            System.out.println(game.getCardMatrix()[GridPane.getRowIndex(card)][GridPane.getColumnIndex(card)]);
            System.out.println(game.getCardMatrix()[GridPane.getRowIndex(card)][GridPane.getColumnIndex(card)].charAt(0) == '0');
            return game.getCardMatrix()[GridPane.getRowIndex(card)][GridPane.getColumnIndex(card)].charAt(0) == '0';
        }

        public Move getMove() {
            return new Move(row1, row2, column1, column2, cardid1, cardid2, login);
        }
    }

    public static class GameListener extends Service {

        protected Task createTask() {

            return new Task<String>() {

                @Override
                protected String call() throws Exception {
                    try {
                        System.out.println("GameListener reacts");
                        game = application.gameChanged(game.getGameId());
                        System.out.println("signal get");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    return "done";
                }
            };
        }
    }

    public class FinishWindow implements Runnable {

        @Override
        public void run() {
            if (game.getStatus().equals("finished")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error message");
                alert.setHeaderText(null);
                if (game.getEndResult() != null && game.getEndResult().get(login) != null) {

                    if (game.getEndResult().get(login).equals("win")) {
                        alert.setContentText("game finished!\n" + "you won!");
                    } else if (game.getEndResult().get(login).equals("lose")) {
                        alert.setContentText("game finished!\n" + "you lost!");
                    } else if (game.getEndResult().get(login).equals("draw")) {
                        alert.setContentText("game finished!\n" + "it's a draw!");
                    }
                } else {
                    alert.setContentText("game finished!");
                }
                alert.showAndWait();

            }
        }
    }



    public class FlipCard extends Transition {

        ImageView card;
        String newId;

        public FlipCard(Duration duration, ImageView card, String newId) {
            setCycleDuration(duration);
            this.card = card;
            this.newId = newId;
        }

        @Override
        protected void interpolate(double frac) {
            card.setImage(new Image(theme.getImage(newId)));

        }
    }
}

