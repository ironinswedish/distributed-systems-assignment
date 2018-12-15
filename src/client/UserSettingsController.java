package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UserSettingsController extends Controller {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label infoLabel;


    public String hashPassword(String password) {

        String generatedPassword="";

        // Create MessageDigest instance for MD5
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            //Add password bytes to digest
            md.update(password.getBytes());

            //Get the hash's bytes
            byte[] bytes = md.digest();

            //This bytes[] has bytes in decimal format;
            //Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
            {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        return generatedPassword;


    }

    public void changeUsername(){
        int result = 0;
        try {
            result = application.changeUsername(usernameField.getText(),login,session);
            System.out.println("Going into change username with: "+usernameField.getText()+ " and  "+login);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(result==-1){
            System.out.println("This username already exists");
            infoLabel.setText("This username already exists");
        }
        else if(result==0){
            System.out.println("Something went wrong.");
        }
        else if(result==-2){
            logOut();
        }
        else{
            login=usernameField.getText();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserSettings.fxml"));
            loader.getNamespace().put("newUsername",login);
            infoLabel.setText("Name succesfully changed!");
        }
    }

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

    public void changePassword(){
        int result = 0;
        try {

            byte[] salt = application.getSalt(login);

            String hashedPassword = getSecurePassword(passwordField.getText(),salt);

            result = application.changePassword(hashedPassword,login,session);
            System.out.println("Going into change password with: "+passwordField.getText()+ " and  "+login);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if(result==0){
            System.out.println("Something went wrong.");
        }
        else if(result==-2){
            logOut();
        }
        else{

            infoLabel.setText("Password succesfully changed!");
        }
    }

    public void backToLobby(){
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

            //Stage stage = (Stage) quitButton.getScene().getWindow();
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
            e.printStackTrace();
        }

    }

}
