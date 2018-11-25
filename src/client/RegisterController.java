package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Random;

public class RegisterController extends Controller{

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private PasswordField confirmedPassword;
    @FXML
    private Button registerKnop;
    @FXML
    private Button backKnop;
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

    //Add salt
    private byte[] getSalt() throws NoSuchAlgorithmException, NoSuchProviderException {
        //Always use a SecureRandom generator
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
        //Create array for salt
        byte[] salt = new byte[16];
        //Get a random salt
        sr.nextBytes(salt);
        //return salt
        return salt;
    }




    public void register() {


        if(!confirmedPassword.getText().equals(password.getText())){
            System.out.println(confirmedPassword+ " en "+password);
            messageLabel.setText("The passwords are not the same.");
        }
        else{
        try {
            login = username.getText();

            byte[] salt = new byte[0];
            try {
                salt = getSalt();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }

            String hashedPassword = getSecurePassword(password.getText(),salt);

            System.out.println(salt);
            System.out.println(hashedPassword);
            String[] result = application.register(login, hashedPassword,salt);

            session = result[1];
            if (result[0].equals("ok")) {
                enterLobby();
            }
            else if(result[0].equals("user exists")){
                messageLabel.setText("This user already exists.");
            }
            else {
                System.out.println(result[0]);
                messageLabel.setText("Something went wrong.");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    }

    public void enterLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            AnchorPane pane = loader.load();
            Controller lobbyController = loader.getController();
            lobbyController.setApplication(application);
            lobbyController.setDispatcher(dispatch);
            lobbyController.setSession(session);
            lobbyController.setLogin(login);
            lobbyController.setStatus(status);
            lobbyController.setStage(stage);

            // Stage stage = (Stage) loginKnop.getScene().getWindow();
            stage.setTitle("Login");
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

}
