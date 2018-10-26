package dispatcher;



import Interfaces.DispatchProtocol;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DispatchProtocolImpl extends UnicastRemoteObject implements DispatchProtocol {


    public DispatchProtocolImpl() throws RemoteException {}

    @Override
    public String[] getApplicationServer() throws RemoteException {
        String[] applicationServer = new String[2];
        applicationServer[0] = "localhost";
        applicationServer[1] = "1399";
        return applicationServer;
    }
}
