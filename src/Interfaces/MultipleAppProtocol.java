package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MultipleAppProtocol extends Remote {

    public boolean getOnline() throws RemoteException;

    public String[] addUser() throws RemoteException;

    public void removeUser() throws RemoteException;

}
