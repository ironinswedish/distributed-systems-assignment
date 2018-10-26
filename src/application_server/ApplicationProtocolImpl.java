package application_server;

import Interfaces.ApplicationProtocol;
import Interfaces.DataBaseProtocol;
import Interfaces.DispatchProtocol;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ApplicationProtocolImpl extends UnicastRemoteObject implements ApplicationProtocol {

    public static Registry databankServer;
    public static DataBaseProtocol dataTransfer;

    public ApplicationProtocolImpl() throws RemoteException {
        setupConnection();
    }

    public void setupConnection(){
        try {
            databankServer = LocateRegistry.getRegistry("localhost", 1499);
            dataTransfer = (DataBaseProtocol) databankServer.lookup("dataBaseService");

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String login(String username, String password) throws RemoteException {
        System.out.println("username: " + username + "password: " + password);
        return dataTransfer.login(username,password);
    }
}
