package client;

import Interfaces.ApplicationProtocol;
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

public class SpectateController extends Controller {

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
                    Game game = application.spectateGame(gameId,login);

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
                    stage.setTitle("Game");
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

                } catch (RemoteException e2) {
                    e2.printStackTrace();
                } catch (IOException e2) {
                    e2.printStackTrace();
                } catch (NotBoundException e1) {
                    e1.printStackTrace();
                }

            });

            this.getChildren().addAll(join, players);
        }
    }

    public void back(){
        try {
            AnchorPane pane = getTransition("Lobby.fxml");

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setTitle("Lobby");
            Scene scene = new Scene(pane);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshListView(){
        List<HBoxCell> gameList = new ArrayList<>();

        try {
            ArrayList<Game> startedGameList = application.getStartedGames();

            Game tempGame;
            for (int i = startedGameList.size()-1; i> -1; i--) {
                tempGame = startedGameList.get(i);
                String[] serverName = tempGame.getApplicatieServer().split("-");
                gameList.add(new HBoxCell("players: " + (tempGame.getCurrentplayer() + 1) + "/" + tempGame.getPlayerCount() + " ", tempGame.getGameId(), Integer.parseInt(serverName[1])));
            }

            ObservableList<HBoxCell> myObservableList = FXCollections.observableList(gameList);
            listView.setItems(myObservableList);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
