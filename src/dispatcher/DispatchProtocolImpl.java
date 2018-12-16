package dispatcher;



import Interfaces.ApplicationProtocol;
import Interfaces.DispatchProtocol;
import Interfaces.MultipleAppProtocol;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DispatchProtocolImpl extends UnicastRemoteObject implements DispatchProtocol {

    static ArrayList<Integer> dblist = new ArrayList<>();
    static ArrayList<Integer> applicationlist = new ArrayList<>();
    static HashMap<Integer, Integer> gameCount = new HashMap<>();
    static HashMap<Integer, Integer> appToDBCount = new HashMap<>();
    static int startDBPort = 14000;
    static int startAppPort = 1499;
    static MultipleAppProtocol multiApp;

    public DispatchProtocolImpl() throws RemoteException {}

    @Override
    public String[] getApplicationServer() throws RemoteException {
        String[] applicationServer = new String[2];


        try {
            Registry appServer = LocateRegistry.getRegistry("localhost", 1399);
            multiApp = (MultipleAppProtocol) appServer.lookup("multipleAppService");

            int port = getBestApp();
            System.out.println("port = " + port);
            int databaseport = getBestDB();
            applicationServer = multiApp.addUser(port,databaseport);
            int currentcount = appToDBCount.get(databaseport);
            currentcount++;
            appToDBCount.replace(databaseport, currentcount);



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

    @Override
    public ArrayList<Integer> registerDB(){
        dblist.add(startDBPort);
        appToDBCount.put(startDBPort, 0);
        startDBPort++;
        return dblist;
    }

    @Override
    public ArrayList<Integer> registerApp(int port){
        applicationlist.add(port);
        gameCount.put(port, 0);
        return applicationlist;
    }

    @Override
    public void incrementGame(int appPort) {
        int currentcount = gameCount.get(appPort);
        currentcount++;
        gameCount.replace(appPort, currentcount);
    }

    @Override
    public void decreaseGame(int appPort, int dbPort) {
        int currentcount = gameCount.get(appPort);
        currentcount--;
        gameCount.replace(appPort, currentcount);
        if (currentcount < 1) {
            try {
                int currentcount2 = appToDBCount.get(dbPort);
                currentcount2--;
                appToDBCount.replace(dbPort, currentcount2);
                multiApp.stopApplicationServer(appPort);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int getBestApp() {
        int port = 0;
        int min = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Integer> entry : gameCount.entrySet()) {
            if(entry.getValue()<2) {
                if (entry.getValue() < min) {
                    port = entry.getKey();
                    min = entry.getValue();
                }
            }
        }
        if (port == 0) {
            port = startAppPort;
            System.out.println(port);
            startAppPort++;
        }
        return port;
    }

    public int getBestDB(){
        int port = 0;
        int min = Integer.MAX_VALUE;
        for (Map.Entry<Integer, Integer> entry : appToDBCount.entrySet()) {
                if (entry.getValue() < min) {
                    port = entry.getKey();
                    min = entry.getValue();
                }
        }
        return port;
    }
}
