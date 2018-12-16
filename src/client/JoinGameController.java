package client;

import Interfaces.ApplicationProtocol;
import Interfaces.DispatchProtocol;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import shared_objects.Game;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

public class JoinGameController extends Controller {

    @FXML
    private ListView<HBoxCell> listView;

    @FXML
    private Button backButton;

    @FXML
    private Button refreshButton;

    public void initialize() {
        refreshListView();

    }

    private class HBoxCell extends HBox {
        Label players = new Label();
        Button join = new Button();

        HBoxCell(String labelText, String gameId, int port) {
            super();
            players.setText(labelText);
            players.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(players, Priority.ALWAYS);

            join.setText("join");
            join.setOnMouseClicked(e -> {
                try {
                    System.out.println(gameId);
                    if (application.getOwnPort()!=port) {

                        Registry registry =  LocateRegistry.getRegistry("localhost", port);
                        application = (ApplicationProtocol) registry.lookup("applicationService");

                    }
                    Game game = application.joinGame(gameId, login, session);

                    if(game==null){
                        logOut();
                    }
                    else{
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Game.fxml"));

                    AnchorPane pane = loader.load();

                    GameController controller = loader.getController();
                    controller.setApplication(application);
                    controller.setStatus(status);
                    controller.setSession(session);
                    controller.setDispatcher(dispatch);
                    controller.setLogin(login);
                    controller.setStage(stage);
                    controller.setGame(game);
                    controller.setCards();
                    stage.setTitle("game");
                    stage.setOnCloseRequest( e2 -> {

                        try {
                            controller.quit();
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
                } catch (RemoteException e2) {
                    e2.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                } catch (NotBoundException e1) {
                    e1.printStackTrace();
                }

            });

            this.getChildren().addAll(players, join);
        }
    }

    public void back(){
        try {
            AnchorPane pane = getTransition("PlaySelect.fxml");

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setTitle("Play selection screen");

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

    public void refreshListView(){
        List<HBoxCell> gameList = new ArrayList<>();

        try {
            ArrayList<Game> pendingGameList = application.getPendingGames();

            Game tempGame;
            for (int i = pendingGameList.size()-1; i> -1; i--) {
                tempGame = pendingGameList.get(i);
                String[] splitServerName = tempGame.getApplicatieServer().split("-");
                gameList.add(new HBoxCell("players: " + (tempGame.getCurrentplayer()+1) + "/" + tempGame.getPlayerCount() + " ", tempGame.getGameId(),Integer.parseInt(splitServerName[1])));
            }

            ObservableList<HBoxCell> myObservableList = FXCollections.observableList(gameList);
            listView.setItems(myObservableList);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
