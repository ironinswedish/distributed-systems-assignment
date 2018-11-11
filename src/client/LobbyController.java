package client;

import Interfaces.ApplicationProtocol;
import Interfaces.DispatchProtocol;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LobbyController {

    private ApplicationProtocol application;
    private static DispatchProtocol dispatch;
    private String session;
    private String status;

    @FXML
    private Button quitButton;


    public void setApplication(ApplicationProtocol application) {
        this.application = application;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public static void setDispatcher(DispatchProtocol dispatch) {
        LobbyController.dispatch = dispatch;
    }


    public static String[] readFromFile() {
        BufferedReader br;
        String[] userinfo = new String[3];

        try {
            br = new BufferedReader(new FileReader("session.txt"));

            String line = br.readLine();

            System.out.println(line);
            if (line != null) {
                userinfo = line.split(" ");
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        return userinfo;
    }
    public void logOut(){
        try {

            application.logout(session,false);
            //dispatch.logout();
            String[] result = readFromFile();
            LoginController.writeToFile(result[0],result[1], 0);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            AnchorPane pane = loader.load();
            loader.getNamespace().put("status", status);

            LoginController loginController = loader.getController();
            loginController.setApplication(application);
            loginController.setStatus(status);
            loginController.setSession(session);
            loginController.setDispatcher(dispatch);

            Stage stage = (Stage) quitButton.getScene().getWindow();
            Scene scene = new Scene(pane);
            stage.setScene(scene);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
