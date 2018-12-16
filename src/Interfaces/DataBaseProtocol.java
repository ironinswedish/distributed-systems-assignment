package Interfaces;

import shared_objects.Game;

import shared_objects.Theme;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface DataBaseProtocol extends Remote {

    public String[] login(String username, String password,String session) throws RemoteException;

    public String[] registerUser(String username, String password, byte[] salt) throws RemoteException;

    public void logout(String login, String session,boolean xButton) throws RemoteException;

    Theme getTheme(String themeName) throws RemoteException;

    public int changeUsername(String usernameField, String login,String session) throws RemoteException;

    int changePassword(String newPassword, String login,String session) throws RemoteException;

    double[] getUserStats(String login,String session) throws RemoteException;

    HashMap<String, Integer> getRanking() throws RemoteException;

    public Game createGame(Game game, String appserver,String login,String session) throws RemoteException;

    //public void setId(Game game, String appserver) throws RemoteException;

    public void updateGame(Game game) throws  RemoteException;

    public ArrayList<Game> getGamesWithStatus(String status) throws RemoteException;

    public void addLoss(String login) throws RemoteException;

    public void addWin(String login) throws RemoteException;

    public void addDraw(String login) throws RemoteException;

    public ArrayList<String> getThemeNames() throws RemoteException;

    public ArrayList<Theme> getPreviewThemes() throws RemoteException;

    List<String> getThemesWithSize(int size) throws RemoteException;

    byte[] getSalt(String login) throws RemoteException;

    Theme getTheme(int themeId) throws RemoteException;

    boolean checkToken(String login, String session);
}
