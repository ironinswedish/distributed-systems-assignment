package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataBaseProtocol extends Remote {

    public String[] login(String username, String password,String session) throws RemoteException;

    public String[] registerUser(String username, String password) throws RemoteException;

    public void logout(String login, String session,boolean xButton) throws RemoteException;
}
