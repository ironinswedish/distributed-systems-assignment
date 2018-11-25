package client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import shared_objects.Person;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

public class RankingsController  extends Controller implements Initializable {

    @FXML private TableView<Person> table;
    @FXML private TableColumn<Person,Integer> colScore;
    @FXML private TableColumn<Person,String> colName;
    @FXML private TableColumn<Person,Integer> colRank;



    public void backToLobby() {
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


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //scoreColumn
        colScore.setCellValueFactory(new PropertyValueFactory<>("Score"));

        //nameColumn
        colName.setCellValueFactory(new PropertyValueFactory<>("Name"));

        colRank.setCellValueFactory(new PropertyValueFactory<>("Rank"));

        try {
            table.setItems(getCharacters());

            ObservableList<Person> chars = getCharacters();
            for(Person p:chars){
                if(p.getName().equals(login)){
                    int toFocus = p.getRank()-1;
                    table.requestFocus();
                    table.getSelectionModel().select(toFocus);
                    table.getFocusModel().focus(0);
                }
            }


        } catch (RemoteException e) {
            e.printStackTrace();
        }





    }

    private ObservableList<Person> getCharacters() throws RemoteException {

        ObservableList<Person> characters = FXCollections.observableArrayList(

        );

        HashMap<String,Integer> ranking = application.getRanking();
        SortedSet<String> keys = new TreeSet<>(ranking.keySet());

        ArrayList<Person> p = new ArrayList<>();

        for (String key : keys) {
            p.add(new Person(ranking.get(key),key));
        }

        Collections.sort(p, new Comparator<Person>() {
            public int compare(Person s1, Person s2) {
                return s2.getScore()-(s1.getScore());
            }
        });

        for(int i=0;i<p.size();i++){
            p.get(i).setRank(i+1);
        }

        for(Person per:p){
            characters.add(new Person(per.getScore(),per.getName(),per.getRank()));
            System.out.println(per.getName()+per.getScore()+""+per.getRank());
        }
        return characters;
    }


    }
