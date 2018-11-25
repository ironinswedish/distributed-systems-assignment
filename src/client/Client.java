package client;

import Interfaces.ApplicationProtocol;
import Interfaces.DispatchProtocol;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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

        loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        pane = loader.load();
        loader.getNamespace().put("status", status);
        if(status.equals("status: connected")){
            loader.getNamespace().put("statusColor","GREEN");
        }
        else{
            loader.getNamespace().put("statusColor","RED");
        }


        LoginController loginController = loader.getController();
        loginController.setApplication(application);
        loginController.setDispatcher(dispatch);
        loginController.setStatus(status);
        loginController.setStage(primaryStage);

        primaryStage.setTitle("Login");


        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);

        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            try {
                if (dispatch != null) {
                    dispatch.logout();
                }

                /*
                if (!primaryStage.getTitle().equals("login")) {
                    //dispatch.logout();

                    application.logout(session, true);
                }*/
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        });


    }

}
