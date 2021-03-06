package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import shared_objects.Person;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

public class LobbyController extends Controller{

    @FXML
    private Button quitButton;

    @FXML
    private Button playButton;

    @FXML
    private Button spectateButton;

    public void PlayMenu() {
        try {

            AnchorPane pane = getTransition("PlaySelect.fxml");

            Stage stage = (Stage) quitButton.getScene().getWindow();
            Scene scene = new Scene(pane);
            stage.setTitle("play selection screen");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void Spectate(){
        try {

            AnchorPane pane = getTransition("SpectateGame.fxml");

            Stage stage = (Stage) quitButton.getScene().getWindow();
            Scene scene = new Scene(pane);
            stage.setTitle("Spectate");
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void enterRankings(){
        try {

            //Retrieve the user stats
            double[] stats =application.getUserStats(login,session);
            if(stats[0]==-2){
                logOut();
            }
            else{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Rankings.fxml"));
            AnchorPane pane = loader.load();
            Controller rankingsController = loader.getController();

            rankingsController.setApplication(application);
            rankingsController.setDispatcher(dispatch);
            rankingsController.setSession(session);
            rankingsController.setLogin(login);
            rankingsController.setStatus(status);
            rankingsController.setStage(stage);


            loader.getNamespace().put("wins","Wins: "+((int)stats[0]));
            loader.getNamespace().put("draws","Draws: "+((int)stats[1]));
            loader.getNamespace().put("losses","Losses: "+((int)stats[2]));

            loader.getNamespace().put("winPerc","Win percentage: "+((int)((stats[0]/(stats[0]+stats[1]+stats[2]))*100))+"%");

            stage.setTitle("Rankings");
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
            }

        } catch (IOException e) {
            if(e.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                reconnect();
            }
        }


    }

    public void enterStats(){
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("UserSettings.fxml"));
            AnchorPane pane = loader.load();
            Controller userStatsController = loader.getController();
            userStatsController.setApplication(application);
            userStatsController.setDispatcher(dispatch);
            userStatsController.setSession(session);
            userStatsController.setLogin(login);
            userStatsController.setStatus(status);
            userStatsController.setStage(stage);

            loader.getNamespace().put("newUsername",login);


            stage.setTitle("Settings");
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
            if(e.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                reconnect();
            }
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
            if(e.getCause().toString().equals("java.net.ConnectException: Connection refused: connect")) {
                reconnect();
            }
            //e.printStackTrace();
        }

    }




}
