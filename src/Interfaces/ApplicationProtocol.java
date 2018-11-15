package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ApplicationProtocol extends Remote {

    public String[] login(String username, String password, String session) throws RemoteException;

    public String[] register(String username, String password) throws RemoteException;

    public void logout(String login, String session,boolean xButton) throws RemoteException;

    public String respond() throws RemoteException;
}
