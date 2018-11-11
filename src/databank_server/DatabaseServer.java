package databank_server;

import dispatcher.DispatchProtocolImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.*;

public class DatabaseServer {


    public static void start() {
        try {
            Registry registry = LocateRegistry.createRegistry(1499);
            registry.rebind("dataBaseService", new DataBaseProtocolImpl());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("Database online");
    }





    public static void main(String[] args){
        start();
    }
}
