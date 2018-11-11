package client;

import Interfaces.ApplicationProtocol;
import Interfaces.DispatchProtocol;


import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ResourceBundle;

public class Client extends Application {

    static ApplicationProtocol application;
    static DispatchProtocol dispatch;

    private static String session;
    private static String status;
    private static String username;


    public static void main(String[] args) {
        launch(args);


    }

    public static void setupConnection() {
        try {
            Registry dispatcher = LocateRegistry.getRegistry("localhost", 1299);
            dispatch = (DispatchProtocol) dispatcher.lookup("dispatchService");
            String[] applicationAdress = dispatch.getApplicationServer();
            Registry applicationServer = LocateRegistry.getRegistry(applicationAdress[0], Integer.parseInt(applicationAdress[1]));
            application = (ApplicationProtocol) applicationServer.lookup("applicationService");
            //System.out.println(application.login("", ""));
            status = "status: connected";
        } catch (RemoteException e) {
            //e.printStackTrace();
            status = "status: not connected";
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        setupConnection();
        FXMLLoader loader;
        AnchorPane pane;
        String result = inSession();

        if (!result.equals("exit")) {
            if (result.equals("Lobby")) {
                loader = new FXMLLoader(getClass().getResource("Lobby.fxml"));
                pane = loader.load();
                LobbyController lobbyController = loader.getController();
                lobbyController.setApplication(application);
                lobbyController.setDispatcher(dispatch);
                lobbyController.setSession(session);
                lobbyController.setStatus(status);

                primaryStage.setTitle("lobby");
            } else {
                loader = new FXMLLoader(getClass().getResource("Login.fxml"));
                pane = loader.load();
                loader.getNamespace().put("status", status);

                LoginController loginController = loader.getController();
                loginController.setApplication(application);
                loginController.setDispatcher(dispatch);
                loginController.setStatus(status);
                loginController.setSession(session);

                primaryStage.setTitle("login");
            }

            Scene scene = new Scene(pane);
            primaryStage.setScene(scene);

            primaryStage.show();
            primaryStage.setOnCloseRequest(e -> {
                try {
                    application.logout(session, true);
                    dispatch.logout();
                    System.out.println(primaryStage.getTitle());
                    if (!primaryStage.getTitle().equals("login")) {
                        application.logout(session,false);
                        //dispatch.logout();
                        String[] sessioninfo = LobbyController.readFromFile();
                        LoginController.writeToFile(sessioninfo[0],sessioninfo[1], 0);
                    }
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            });
        }

    }

    public String inSession() throws RemoteException {

        if (readSession().equals("1")) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error message");
            alert.setHeaderText(null);
            alert.setContentText("application allready running");
            alert.showAndWait();
            return "exit";
        }
        if (username.equals("")) {
            return "login";
        } else {
            String[] result = application.login(username, "", session);
            session = result[1];
            if (result[0].equals("ok")) {
                return "lobby";
            } else if (result[1].equals("user allready online")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error message");
                alert.setHeaderText(null);
                alert.setContentText("user allready online");
                alert.showAndWait();
            }
            return "login";
        }

    }

    public String readSession() {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("session.txt"));

            String line = br.readLine();

            System.out.println(line);
            if (line != null) {
                String[] userinfo = line.split(" ");
                username = userinfo[0];
                if (userinfo.length == 3) {
                    session = userinfo[1];
                    return userinfo[2];
                }
            } else {
                username = "";
                session = "";
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0";
    }
}
