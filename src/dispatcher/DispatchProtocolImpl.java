package dispatcher;



import Interfaces.ApplicationProtocol;
import Interfaces.DataBaseProtocol;
import Interfaces.DispatchProtocol;
import Interfaces.MultipleAppProtocol;

import java.rmi.AccessException;
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
    static HashMap<Integer, DataBaseProtocol> databaseAccesMap = new HashMap<>();

    public DispatchProtocolImpl() throws RemoteException {}

    /**
     * aanmaken van applicatieserver of toekennen van bestepassende applicatieserver aan client
     * een lijst met de host en het poortnummer wordt teruggestuurd naar de client.
     * @return
     * @throws RemoteException
     */
    @Override
    public String[] getApplicationServer() throws RemoteException {
        String[] applicationServer = new String[2];


        try {
            Registry appServer = LocateRegistry.getRegistry("localhost", 1399);
            multiApp = (MultipleAppProtocol) appServer.lookup("multipleAppService");

            int port = getBestApp();
            System.out.println("appport = " + port);
            int databaseport = getBestDB();
            System.out.println("dbport = "+databaseport);
            applicationServer = multiApp.addUser(port,databaseport);




        } catch (NotBoundException e) {
            e.printStackTrace();
        }


        return applicationServer;
    }

    /**
     * uitloggen
     * @throws RemoteException
     */
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

    /**
     * wanneer een database aangemaakt wordt moet deze zich hier registreren en krijgt deze een poortnummer om op te werken en een lijst met alle andere databaseservers
     * @return
     */
    @Override
    public ArrayList<Integer> registerDB(){
        dblist.add(startDBPort);
        appToDBCount.put(startDBPort, 0);
        startDBPort++;
        if(dblist.size()>1) {
            updateDataBases();
        }
        return dblist;
    }

    /**
     * de appserver kan registreert zich bij het aanmaken en krijgt een lijst met alle appserverpoorten terug.
     * @param port
     * @return
     */
    @Override
    public ArrayList<Integer> registerApp(int port,int databaseport){
        applicationlist.add(port);
        gameCount.put(port, 0);
        int currentcount = appToDBCount.get(databaseport);
        currentcount++;
        appToDBCount.replace(databaseport, currentcount);
        return applicationlist;
    }

    /**
     * wanneer een game aangemaakt wordt in de appserver wordt de dispatcher hiervan verwittigd
     * @param appPort
     */
    @Override
    public void incrementGame(int appPort) {
        int currentcount = gameCount.get(appPort);
        currentcount++;
        gameCount.replace(appPort, currentcount);
        System.out.println("game increased"+appPort);
    }

    /**
     * wanneer een game gedaan is wordt de appserver verwittigd
     * @param appPort
     * @param dbPort
     */
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
        System.out.println("game decreased"+appPort);
    }

    /**
     * ophalen van beste appserver poortnummer gebasseerd op aantal games elke server bevat
     * @return
     */
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

    /**
     * beste databasepoortnummer meegeven gebaseerd op database met minste appservers
     * @return
     */
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

    private void updateDataBases(){

        DataBaseProtocol dataProt;
        Registry registry;
        for (Integer port : dblist) {
            if (port != dblist.get(dblist.size() - 1)) {
                try {
                    dataProt = databaseAccesMap.get(port);
                    if (dataProt == null) {
                        registry = LocateRegistry.getRegistry("localhost", port);
                        databaseAccesMap.put(port, (DataBaseProtocol) registry.lookup("dataBaseService"));

                    }
                    databaseAccesMap.get(port).updateDBList(dblist);
                } catch (AccessException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
