package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MultipleAppProtocol extends Remote {

    public String[] addUser(int port, int databasePort) throws RemoteException;

    public void removeUser() throws RemoteException;

    public void stopApplicationServer(int port) throws RemoteException;



}
