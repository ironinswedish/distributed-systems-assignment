package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ApplicationProtocol extends Remote {

    public String login(String username, String password) throws RemoteException;
}
