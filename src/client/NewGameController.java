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
import javafx.stage.Stage;
import shared_objects.Game;
import shared_objects.Theme;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class NewGameController extends Controller {

    private List<String> themeNames;
    private HashMap<String, Theme> previewThemes;

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

    @FXML
    private Button backButton;

    private final ToggleGroup playerCount;
    private final ToggleGroup gridSize;


    public void createGame() {
        System.out.println(playerCount.getSelectedToggle().getUserData().toString());
        System.out.println(gridSize.getSelectedToggle().getUserData().toString());
        System.out.println("game created");
        System.out.println(choiceBox.getValue());
        System.out.println("test 2 2 2 2 ");

       /* Theme chosenTheme = null;
        try {
            chosenTheme = application.getTheme(choiceBox.getValue());
            System.out.println(chosenTheme.getCardMap().size()+ "is size van het gekozen theme");
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
        String playerTotal = playerCount.getSelectedToggle().getUserData().toString();
        String gridTotal = gridSize.getSelectedToggle().getUserData().toString();
        //Game game = new Game(Integer.parseInt(playerTotal),Integer.parseInt(gridTotal), chosenTheme);

        try {
            Game game = application.createGame(Integer.parseInt(playerTotal), login, Integer.parseInt(gridTotal), choiceBox.getValue(),session);

            if(game==null){
                System.out.println("game is null");
                logOut();
            }
            else{
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
            controller.setTheme();
            controller.setCards();

            stage.setTitle("Game");
            stage.setOnCloseRequest(e2 -> {

                try {
                    controller.quit();
                    if (dispatch != null) {
                        dispatch.logout();
                    }
                    if (application != null) {
                        application.logout(login, session, true);
                    }

                } catch (RemoteException e1) {
                    if(e1.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                        reconnect();
                    }
                    e1.printStackTrace();
                }

            });

            Scene scene = new Scene(pane);
            stage.setScene(scene);
            }
        } catch (RemoteException e) {
            if(e.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                reconnect();
            }
            e.printStackTrace();
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
        getThemes();

        setPics(previewThemes.get(themeNames.get(0)));
    }

    public void getThemes() {
        previewThemes = new HashMap<String, Theme>();
        try {
            ArrayList<Theme> previewThemesList = application.getPreviewThemes();
            int i = 0;
            for (Theme theme : previewThemesList) {
                previewThemes.put(themeNames.get(i), theme);
                i++;
            }
        } catch (RemoteException e) {
            if(e.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                reconnect();
            }
            e.printStackTrace();
        }
    }

    public void logOut() {
        try {

            application.logout(login, session, false);
            //dispatch.logout();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            AnchorPane pane = loader.load();
            loader.getNamespace().put("status", status);

            Controller loginController = loader.getController();
            loginController.setApplication(application);
            loginController.setStatus(status);
            loginController.setSession(session);
            loginController.setDispatcher(dispatch);
            loginController.setStage(stage);

            Scene scene = new Scene(pane);
            stage.setTitle("Login");
            stage.setOnCloseRequest(e -> {
                try {

                    dispatch.logout();
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            });
            stage.setScene(scene);


        } catch (IOException e) {
            if(e.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                reconnect();
            }
            e.printStackTrace();
        }

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
        try {
            themeNames=application.getThemesWithSize(8);
        } catch (RemoteException e) {
            System.out.println(e.getCause());
            System.out.println("application connection refused");
            if(e.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                reconnect();
            }
            e.printStackTrace();
        }
        choiceBox.setItems(FXCollections.observableArrayList(themeNames));
        choiceBox.setValue(themeNames.get(0));
        choiceBox.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (ov, oldSelected, newSelected) -> {
            getPreviewPics(Integer.parseInt(newSelected.toString()));

        });
    }

    public void setChoiceBox4(){
        try {
            themeNames=application.getThemesWithSize(8);
        } catch (RemoteException e) {
            if(e.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                reconnect();
            }
        }
        choiceBox.setItems(FXCollections.observableArrayList(themeNames));
        choiceBox.setValue(themeNames.get(0));
        choiceBox.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (ov, oldSelected, newSelected) -> {
            getPreviewPics(Integer.parseInt(newSelected.toString()));

        });
    }

    public void setChoiceBox6(){
        try {
            themeNames=application.getThemesWithSize(18);
        } catch (RemoteException e) {
            if(e.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                reconnect();
            }
        }
        choiceBox.setItems(FXCollections.observableArrayList(themeNames));
        choiceBox.setValue(themeNames.get(0));
        choiceBox.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (ov, oldSelected, newSelected) -> {
            getPreviewPics(Integer.parseInt(newSelected.toString()));

        });
    }

    public void setChoiceBox8(){
        try {
            themeNames=application.getThemesWithSize(32);
        } catch (RemoteException e) {
            if(e.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                reconnect();
            }
            e.printStackTrace();
        }
        choiceBox.setItems(FXCollections.observableArrayList(themeNames));
        choiceBox.setValue(themeNames.get(0));
        choiceBox.getSelectionModel().selectedIndexProperty().addListener((ChangeListener) (ov, oldSelected, newSelected) -> {
            getPreviewPics(Integer.parseInt(newSelected.toString()));

        });
    }

    public void updateThemes() {
        System.out.println("updating themes");
        try {
            themeNames = application.getThemeNames();
        } catch (RemoteException e) {
            if(e.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                reconnect();
            }
            e.printStackTrace();
        }
        /*themeList.add("Shiba's");
        themeList.add("Star Wars");
        themeList.add("Pokémon");*/
    }

    public void getPreviewPics(int themeNumber) {
        String theme = themeNames.get(themeNumber);
        setPics(previewThemes.get(theme));
    }

    public void setPics(Theme theme) {

        Image backimage = new Image(theme.getImage("0"));
        Image preview1 = new Image(theme.getImage("1"));
        Image preview2 = new Image(theme.getImage("2"));
        Image preview3 = new Image(theme.getImage("3"));

        leftUpperImage.setImage(backimage);
        leftLowerImage.setImage(preview1);
        rightLowerImage.setImage(preview2);
        rightUpperImage.setImage(preview3);

    }

    public void back() {
        try {
            AnchorPane pane = getTransition("PlaySelect.fxml");

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(pane);
            stage.setTitle("Play selection screen");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
