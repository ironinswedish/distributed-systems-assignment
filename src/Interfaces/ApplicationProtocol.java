package Interfaces;

import javafx.scene.control.TextField;
import shared_objects.Theme;

import shared_objects.Game;
import shared_objects.Move;
import shared_objects.Theme;

import java.lang.reflect.Array;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public interface ApplicationProtocol extends Remote {

    public String[] login(String username, String password, String session) throws RemoteException;

    public String[] register(String username, String password) throws RemoteException;

    public void logout(String login, String session,boolean xButton) throws RemoteException;

    public String respond() throws RemoteException;

    public Game createGame(int playerTotal, String login, int gridTotal, String chosenThemeName) throws RemoteException;

    public Game processTurn(Move lastmove, String login, String gameId) throws RemoteException;

    public Game gameChanged(String gameId) throws RemoteException, InterruptedException;

    public Game joinGame(String gameId, String login) throws RemoteException;

    public ArrayList<Game> getPendingGames() throws RemoteException;

    public void quitGame(Game game, String login) throws RemoteException;

    public ArrayList<Game> getStartedGames() throws RemoteException;

    public Game spectateGame(String gameId, String login) throws RemoteException;

    int changeUsername(String usernameField, String login)throws RemoteException;

    int changePassword(String newPassword, String login) throws RemoteException;

    double[] getUserStats(String login) throws RemoteException;

    HashMap<String, Integer> getRanking() throws  RemoteException;

    public ArrayList<String> getThemeNames() throws RemoteException;

    ArrayList<Theme> getPreviewThemes() throws RemoteException;

    List<String> getThemesWithSize(int size) throws RemoteException;
}
