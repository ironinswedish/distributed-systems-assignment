package Interfaces;

import shared_objects.Theme;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface DataBaseProtocol extends Remote {

    public String[] login(String username, String password,String session) throws RemoteException;

    public String[] registerUser(String username, String password) throws RemoteException;

    public void logout(String login, String session,boolean xButton) throws RemoteException;

    Theme getTheme(String themeName) throws RemoteException;

    public int changeUsername(String usernameField, String login) throws RemoteException;

    int changePassword(String newPassword, String login) throws RemoteException;

    double[] getUserStats(String login) throws RemoteException;

    HashMap<String, Integer> getRanking() throws RemoteException;
}
