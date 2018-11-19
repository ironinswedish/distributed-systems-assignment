package Interfaces;

import javafx.scene.control.TextField;
import shared_objects.Theme;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface ApplicationProtocol extends Remote {

    public String[] login(String username, String password, String session) throws RemoteException;

    public String[] register(String username, String password) throws RemoteException;

    public void logout(String login, String session,boolean xButton) throws RemoteException;

    public String respond() throws RemoteException;

    Theme getTheme(String value) throws RemoteException;

    int changeUsername(String usernameField, String login)throws RemoteException;

    int changePassword(String newPassword, String login) throws RemoteException;

    double[] getUserStats(String login) throws RemoteException;

    HashMap<String, Integer> getRanking() throws  RemoteException;
}
