package client;

import Interfaces.ApplicationProtocol;
import Interfaces.DispatchProtocol;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class PlayController extends Controller {

    @FXML
    private Button newButton;

    @FXML
    private Button joinButton;

    @FXML
    private Button backButton;


    public void NewGameMenu() {
        try {


            AnchorPane pane = getTransition("NewGameMenu.fxml");


            Scene scene = new Scene(pane);
            stage.setScene(scene);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void JoinGameMenu() {
        try {


            AnchorPane pane = getTransition("JoinGameMenu.fxml");

            Stage stage = (Stage) joinButton.getScene().getWindow();
            stage.setTitle("join game");

            Scene scene = new Scene(pane);
            stage.setScene(scene);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Back() {
        try {
            AnchorPane pane = getTransition("Lobby.fxml");

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setTitle("lobby");
            
            Scene scene = new Scene(pane);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
