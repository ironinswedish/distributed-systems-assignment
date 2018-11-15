package client;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import shared_objects.Game;
import shared_objects.Theme;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static java.lang.Thread.sleep;


public class GameController extends Controller {


    private Game game;
    private Theme theme;
    private int imageSize;
    final GameJudge gamejudge = new GameJudge();
    int score = 0;

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

    public void setCards() throws FileNotFoundException {
        String matrixSlot;

        imageSize = 85;

        for (int i = 0; i < game.getCardMatrix().length; i++) {

            for (int j = 0; j < game.getCardMatrix()[i].length; j++) {
                matrixSlot = game.getCardMatrix()[i][j];
                final String id = matrixSlot.substring(1);
                final ImageView card;
                System.out.println(id);
                if (matrixSlot.charAt(0) == '0') {
                    card = new ImageView(new Image(new FileInputStream(theme.getImage("0"))));
                    card.setOnMouseClicked(event -> {
                        try {
                            if (gamejudge.turn) {


                                if (gamejudge.counter > 0) {
                                    if (card != gamejudge.card1 &&  gamejudge.cardSelect(card)) {
                                        card.setImage(new Image(new FileInputStream(theme.getImage(id))));
                                        if (gamejudge.counter == 2) {
                                            gamejudge.cardid1 = Integer.parseInt(id);
                                            gamejudge.card1 = card;
                                            gamejudge.row1 = GridPane.getRowIndex(card);
                                            gamejudge.column1 = GridPane.getColumnIndex(card);
                                        } else if (gamejudge.counter == 1) {
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


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });

                    System.out.println("first char is 0");
                } else {


                    card = new ImageView(new Image(new FileInputStream(theme.getImage(id))));

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

    public void setGame(Game game) {
        this.game = game;
        this.theme = game.getTheme();
    }

    public void nextTurn() {
        if (gamejudge.match()) {
            System.out.println("nice its a match");

            score++;
            player1.setText("player1: " + score);

        } else {
            System.out.println("oh ow wrong");
            gamejudge.resetChoices();
        }
        gamejudge.resetJudge();
    }

    private class GameJudge {

        public boolean turn;
        public int counter;
        public int cardid1;
        public int cardid2;
        public ImageView card1;
        public ImageView card2;
        public int row1;
        public int row2;
        public int column1;
        public int column2;


        public GameJudge() {
            turn = true;
            counter = 2;
        }

        public boolean match() {
            if(cardid1 == cardid2){
                game.getCardMatrix()[row1][column1] = "1" + String.valueOf(cardid1);
                game.getCardMatrix()[row2][column2] = "1" + String.valueOf(cardid2);
            }

            return cardid1 == cardid2;
        }

        public void resetJudge() {
            turn = true;
            counter = 2;
            cardid1 = 0;
            cardid2 = 0;
            card2 = null;
            card1 = null;
            row1 = 0;
            row2 = 0;
            column1 = 0;
            column2 = 0;
        }

        public void resetChoices() {
            try {
                card1.setImage(new Image(new FileInputStream(theme.getImage("0"))));
                card2.setImage(new Image(new FileInputStream(theme.getImage("0"))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public boolean cardSelect(ImageView card){
            System.out.println(GridPane.getRowIndex(card));
            System.out.println(GridPane.getColumnIndex(card));
            System.out.println(game.getCardMatrix()[GridPane.getRowIndex(card)][GridPane.getColumnIndex(card)]);
            System.out.println(game.getCardMatrix()[GridPane.getRowIndex(card)][GridPane.getColumnIndex(card)].charAt(0) == '0');
            return game.getCardMatrix()[GridPane.getRowIndex(card)][GridPane.getColumnIndex(card)].charAt(0) =='0';
        }

    }

}
