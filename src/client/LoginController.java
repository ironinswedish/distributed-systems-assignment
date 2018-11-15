package client;

import Interfaces.ApplicationProtocol;
import Interfaces.DispatchProtocol;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;

public class LoginController extends Controller{

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Button loginKnop;
    @FXML
    private Button registerKnop;
    @FXML
    private Button quitKnop;
    @FXML
    private static Label statusLabel;

    public String hashPassword(String password) {
        // hashfunctie
        return password;
    }

    public void login() {
        try {
            System.out.println("Username: " + username.getText() + " Password: " + password.getText());
            login = username.getText();
            String hashedPassword = hashPassword(password.getText());
            String[] result = application.login(login, hashedPassword,session);
            session = result[1];
            if (result[0].equals("ok")) {
                enterLobby();
            } else {
                errorMessage(result[0]);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void register() {

        try {
            login = username.getText();
            String hashedPassword = hashPassword(password.getText());
            String[] result = application.register(login, hashedPassword);
            session = result[1];
            if (result[0].equals("ok")) {
                enterLobby();
            } else {
                errorMessage(result[0]);

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void enterLobby() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Lobby.fxml"));
            AnchorPane pane = loader.load();
            Controller lobbyController = loader.getController();
            lobbyController.setApplication(application);
            lobbyController.setDispatcher(dispatch);
            lobbyController.setSession(session);
            lobbyController.setLogin(login);
            lobbyController.setStatus(status);
            lobbyController.setStage(stage);

            // Stage stage = (Stage) loginKnop.getScene().getWindow();
            stage.setTitle("lobby");
            stage.setOnCloseRequest( e -> {
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

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void errorMessage(String result) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error message");
        alert.setHeaderText(null);
        alert.setContentText(result);
        alert.showAndWait();
    }

    public void quit() {
        Stage stage = (Stage) loginKnop.getScene().getWindow();
        try {
            if (dispatch != null) {
                dispatch.logout();
            }
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
        stage.close();
    }

}
