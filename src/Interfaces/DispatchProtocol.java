package Interfaces;


import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface DispatchProtocol extends Remote {

    public String[] getApplicationServer() throws RemoteException;

    public void logout() throws RemoteException;

    public ArrayList<Integer> registerDB() throws RemoteException;

    public ArrayList<Integer> registerApp(int port) throws RemoteException;

    public void incrementGame(int port) throws RemoteException;

    public void decreaseGame(int port, int dbPort) throws RemoteException;
}
