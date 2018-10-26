package Interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DispatchProtocol extends Remote {

    public String[] getApplicationServer() throws RemoteException;
}
