package client;

import Interfaces.ApplicationProtocol;
import Interfaces.DispatchProtocol;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client extends Application {

    static ApplicationProtocol application;

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Button loginKnop;

    public static void main(String[] args) {
        launch(args);

    }

    public static void setupConnection(){
        try {
            Registry dispatcher = LocateRegistry.getRegistry("localhost", 1299);
            DispatchProtocol dispatch = (DispatchProtocol) dispatcher.lookup("dispatchService");
            String[] applicationAdress = dispatch.getApplicationServer();
            Registry applicationServer = LocateRegistry.getRegistry(applicationAdress[0], Integer.parseInt(applicationAdress[1]));
            application = (ApplicationProtocol) applicationServer.lookup("applicationService");
            System.out.println(application.login("", ""));

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        setupConnection();
        Parent root = FXMLLoader.load(getClass().getResource("client_gui.fxml"));
        primaryStage.setTitle("login");
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void login(){
        try {
            System.out.println("Username: " + username.getText() + " Password: " + password.getText());
            System.out.println(application.login(username.getText(), password.getText()));

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
