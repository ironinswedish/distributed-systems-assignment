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

public class LoginController {

    static ApplicationProtocol application;
    static DispatchProtocol dispatch;
    private static String status;
    private String session;

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


    public void setApplication(ApplicationProtocol application) {
        this.application = application;
    }

    public void setDispatcher(DispatchProtocol dispatch) {
        this.dispatch = dispatch;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSession(String session) {
        this.session = session;
    }




    public String hashPassword(String password) {
        // hashfunctie
        return password;
    }

    public static void writeToFile(String username, String session, int online) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File("session.txt")));
            writer.write(username + " " + session+ " "+ String.valueOf(online));
            System.out.println(username + " " + session+ " "+ online);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void login() {
        try {
            System.out.println("Username: " + username.getText() + " Password: " + password.getText());
            String hashedPassword = hashPassword(password.getText());
            String[] result = application.login(username.getText(), hashedPassword,session);
            session = result[1];
            if (result[0].equals("ok")) {
                writeToFile(username.getText(), session,1);
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
            String hashedPassword = hashPassword(password.getText());
            String[] result = application.register(username.getText(), hashedPassword);
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
            LobbyController lobbyController = loader.getController();
            lobbyController.setApplication(application);
            lobbyController.setDispatcher(dispatch);
            lobbyController.setSession(session);
            lobbyController.setStatus(status);

            Stage stage = (Stage) loginKnop.getScene().getWindow();
            stage.setTitle("lobby");
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
            dispatch.logout();
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
        stage.close();
    }

}
