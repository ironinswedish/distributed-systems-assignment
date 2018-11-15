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
import java.rmi.RemoteException;

public class LobbyController extends Controller{

    @FXML
    private Button quitButton;

    @FXML
    private Button playButton;

    public void PlayMenu() {
        try {

            AnchorPane pane = getTransition("PlaySelect.fxml");

            Stage stage = (Stage) quitButton.getScene().getWindow();
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

            Stage stage = (Stage) quitButton.getScene().getWindow();
            Scene scene = new Scene(pane);
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
