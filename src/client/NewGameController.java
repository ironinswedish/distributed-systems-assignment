package client;

import Interfaces.ApplicationProtocol;
import Interfaces.DispatchProtocol;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import shared_objects.Game;
import shared_objects.Theme;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


public class NewGameController extends Controller {

    private List<String> themeList;

    @FXML
    private RadioButton playercount1;
    @FXML
    private RadioButton playercount2;
    @FXML
    private RadioButton playercount3;
    @FXML
    private RadioButton playercount4;
    @FXML
    private RadioButton gridSize4;
    @FXML
    private RadioButton gridSize6;
    @FXML
    private RadioButton gridSize8;
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private ImageView leftUpperImage;
    @FXML
    private ImageView leftLowerImage;
    @FXML
    private ImageView rightUpperImage;
    @FXML
    private ImageView rightLowerImage;
    @FXML
    private Button createButton;

    private final ToggleGroup playerCount;
    private final ToggleGroup gridSize;


    public void createGame() {
        System.out.println(playerCount.getSelectedToggle().getUserData().toString());
        System.out.println(gridSize.getSelectedToggle().getUserData().toString());
        System.out.println("game created");
        System.out.println(choiceBox.getValue());
        System.out.println("test 2 2 2 2 ");

        Theme chosenTheme = null;
        try {
            chosenTheme = application.getTheme(choiceBox.getValue());
            System.out.println(chosenTheme.getCardMap().size()+ "is size van het gekozen theme");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String playerTotal = playerCount.getSelectedToggle().getUserData().toString();
        String gridTotal = gridSize.getSelectedToggle().getUserData().toString();
        Game game = new Game(Integer.parseInt(playerTotal),Integer.parseInt(gridTotal), chosenTheme);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Game.fxml"));

            AnchorPane pane = loader.load();

            GameController controller = loader.getController();
            controller.setApplication(application);
            controller.setStatus(status);
            controller.setSession(session);
            controller.setDispatcher(dispatch);
            controller.setLogin(login);
            controller.setStage(stage);
            controller.setGame(game);
            System.out.println(chosenTheme.getSize()+ " size die we doorgeven");
            System.out.println("size van het thema: "+chosenTheme.getCardMap().size());
            controller.setCards(chosenTheme);

            Scene scene = new Scene(pane);
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public NewGameController() {
        playerCount = new ToggleGroup();
        gridSize = new ToggleGroup();
    }

    public void initialize() {

        setGroups();
        setChoicebox();
        setPics("memory_resources/shiba/shibamemory");
    }

    public void setGroups() {
        System.out.println("it worked");

        playercount1.setUserData("1");
        playercount2.setUserData("2");
        playercount3.setUserData("3");
        playercount4.setUserData("4");
        playercount1.setToggleGroup(playerCount);
        playercount2.setToggleGroup(playerCount);
        playercount3.setToggleGroup(playerCount);
        playercount4.setToggleGroup(playerCount);
        playercount1.setSelected(true);

        gridSize4.setUserData("4");
        gridSize6.setUserData("6");
        gridSize8.setUserData("8");


        gridSize4.setToggleGroup(gridSize);
        gridSize6.setToggleGroup(gridSize);
        gridSize8.setToggleGroup(gridSize);
        gridSize4.setSelected(true);
    }

    public void setChoicebox() {
        updateThemes();
        choiceBox.setItems(FXCollections.observableArrayList(themeList));
        choiceBox.setValue(themeList.get(0));
        choiceBox.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (ov, oldSelected, newSelected) -> {
            getPreviewPics(Integer.parseInt(newSelected.toString()));

        });
    }

    public void updateThemes() {
        System.out.println("updating themes");
        themeList = new ArrayList<String>();
        themeList.add("Shiba's");
        themeList.add("Star Wars");
        themeList.add("Pokémon");
    }

    public void getPreviewPics(int themeNumber) {
        String theme = themeList.get(themeNumber);
        if (theme.equals("shiba's")) {
            setPics("memory_resources/shiba/shibamemory");
        } else if (theme.equals("star wars")) {
            setPics("memory_resources/star_wars/star_wars");
        } else if (theme.equals("pokémon")) {
            setPics("memory_resources/pokemon/pokemon");
        }
    }

    public void setPics(String path) {
        try {
            Image backimage = new Image(new FileInputStream(path + "_backside.jpg"));
            Image preview1 = new Image(new FileInputStream(path + "1.jpg"));
            Image preview2 = new Image(new FileInputStream(path + "2.jpg"));
            Image preview3 = new Image(new FileInputStream(path + "3.jpg"));

            leftUpperImage.setImage(backimage);
            leftLowerImage.setImage(preview1);
            rightLowerImage.setImage(preview2);
            rightUpperImage.setImage(preview3);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
