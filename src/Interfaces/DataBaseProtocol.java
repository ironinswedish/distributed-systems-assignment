package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataBaseProtocol extends Remote {

    public String login(String username, String password) throws RemoteException;

    public String registerUser(String username, String password) throws RemoteException;
}
