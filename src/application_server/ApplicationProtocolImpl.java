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
    public void logout(String login, String session, boolean xButton) throws RemoteException {
        System.out.println("user logged out");
        dataTransfer.logout(login,session,xButton);
    }

    @Override
    public String respond() throws RemoteException {
        return "hello";
    }

    @Override
    public String[] login(String username, String password, String session) throws RemoteException {
        System.out.println("username: " + username + "password: " + password);
        return dataTransfer.login(username,password,session);
    }

    @Override
    public String[] register(String username, String password) throws RemoteException {
        System.out.println("username: " + username + "password: " + password);
        return dataTransfer.registerUser(username,password);
    }
}
