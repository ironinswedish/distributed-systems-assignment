package databank_server;

import Interfaces.DataBaseProtocol;
import Interfaces.DispatchProtocol;
import dispatcher.DispatchProtocolImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseServer {

    static DispatchProtocol dispatch;
    static int port;
    static ArrayList<Integer> databaseList;
    static DataBaseProtocol dataprot;

    public static void start() {
        try {
            Registry dispatcher = LocateRegistry.getRegistry("localhost", 1299);
            dispatch = (DispatchProtocol) dispatcher.lookup("dispatchService");
            databaseList = dispatch.registerDB();
            port = databaseList.get(databaseList.size() - 1);
            Registry registry = LocateRegistry.createRegistry(port);
            System.out.println("registered database to port" + port);
            registry.rebind("dataBaseService", new DataBaseProtocolImpl(databaseList));
            dataprot = (DataBaseProtocol) registry.lookup("dataBaseService");
            dataprot.initialSetup();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        System.out.println("Database online");
    }





    public static void main(String[] args){
        start();
    }
}
