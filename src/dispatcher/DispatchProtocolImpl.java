package dispatcher;



import Interfaces.DispatchProtocol;
import Interfaces.MultipleAppProtocol;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DispatchProtocolImpl extends UnicastRemoteObject implements DispatchProtocol {


    public DispatchProtocolImpl() throws RemoteException {}

    @Override
    public String[] getApplicationServer() throws RemoteException {
        String[] applicationServer = new String[2];


        try {
            Registry appServer = LocateRegistry.getRegistry("localhost", 1399);
            MultipleAppProtocol multiApp = (MultipleAppProtocol) appServer.lookup("multipleAppService");

            applicationServer = multiApp.addUser();



        } catch (NotBoundException e) {
            e.printStackTrace();
        }


        return applicationServer;
    }

    @Override
    public void logout() throws RemoteException {

        try {
            Registry appServer = LocateRegistry.getRegistry("localhost", 1399);
            MultipleAppProtocol multiApp = (MultipleAppProtocol) appServer.lookup("multipleAppService");
            System.out.println("user left");
            multiApp.removeUser();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }


}
