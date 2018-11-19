package client;

import Interfaces.ApplicationProtocol;
import Interfaces.DispatchProtocol;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import shared_objects.Person;

import java.io.IOException;
import java.util.ArrayList;

public class Controller {

    static ApplicationProtocol application;
    static DispatchProtocol dispatch;
    public static String status;
    public String session;
    public String login;


    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage stage;


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

    public String getSession() {
        return this.session;
    }

    public String getLogin() {
        return this.login;
    }
    public void setLogin(String login) {
        this.login = login;
    }

    public AnchorPane getTransition(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));

        AnchorPane pane = loader.load();

        Controller controller = loader.getController();
        controller.setApplication(application);
        controller.setStatus(status);
        controller.setSession(session);
        controller.setDispatcher(dispatch);
        controller.setLogin(login);
        controller.setStage(stage);


        return pane;
    }

}
