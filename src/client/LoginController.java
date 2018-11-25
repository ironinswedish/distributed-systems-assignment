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

import java.io.*;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

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
    @FXML
    private Label messageLabel;

    private String getSecurePassword(String passwordToHash, byte[] salt)
    {
        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(salt);
            //Get the hash's bytes
            byte[] bytes = md.digest(passwordToHash.getBytes());
            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }


    public void login() {
        try {
            System.out.println("Username: " + username.getText() + " Password: " + password.getText());
            login = username.getText();

            byte[] salt =application.getSalt(login);


            String hashedPassword = getSecurePassword(password.getText(),salt);

            System.out.println(salt);
            System.out.println(hashedPassword);
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

    public void enterRegister(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Register.fxml"));
            AnchorPane pane = loader.load();
            Controller lobbyController = loader.getController();
            lobbyController.setApplication(application);
            lobbyController.setDispatcher(dispatch);
            lobbyController.setSession(session);
            lobbyController.setLogin(login);
            lobbyController.setStatus(status);
            lobbyController.setStage(stage);

            // Stage stage = (Stage) loginKnop.getScene().getWindow();
            stage.setTitle("Register");
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
            stage.setTitle("Lobby");
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
