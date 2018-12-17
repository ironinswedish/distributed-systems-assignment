package application_server;

import Interfaces.DispatchProtocol;
import Interfaces.MultipleAppProtocol;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

/*
protocol verantwoordelijk voor communicatie tussen ApplicationServers
 */
public class MultipleAppProtocolImpl extends UnicastRemoteObject implements MultipleAppProtocol {

    private int userCount;
    private Registry dispatcher;
    private Registry registry;
    private String[] applicationServer;
    private HashMap<Integer, Registry> appserverMap = new HashMap<>();
    private DispatchProtocol dispatchProtocol;

    private ArrayList<Integer> onlinePorts = new ArrayList<>();


    protected MultipleAppProtocolImpl() throws RemoteException {

    }

    // startApplicationSevrever en stopApplicationServer zijn methodes voor het dynamisch bij creeren van appServers wanneer we gedistrubueerd gaan werken
    //deze methode start de effectieve applicatieServer op een andere poort
    //zet de host en het addres vast
    private String[] startApplicationServer(int port,int databasePort) throws RemoteException {

        String serverport = ""+port;
        String host = "localhost";
        applicationServer = new String[2];
        applicationServer[0] = "";
        applicationServer[1] = "";
        try {

           /* String[] serviceList = registry.list();
            for (int i = 0; i < serviceList.length; i++) {
                if (serviceList[i].equals("applicationService")) {
                    bound = true;
                }
            }*/
            try {
                if (onlinePorts.isEmpty()) {
                    dispatcher = LocateRegistry.getRegistry("localhost", 1299);
                    dispatchProtocol = (DispatchProtocol) dispatcher.lookup("dispatchService");
                }
                if (!onlinePorts.contains(port)) {
                    System.out.println("port = " + port);
                    registry = LocateRegistry.createRegistry(port);
                    registry.rebind("applicationService", new ApplicationProtocolImpl(dispatchProtocol.registerApp(port,databasePort),databasePort));
                    onlinePorts.add(port);
                    appserverMap.put(port, registry);
                } else {

                }
            } catch (NotBoundException e) {
                e.printStackTrace();
            }

            applicationServer[0] = host;
            applicationServer[1] = serverport;


            System.out.println("ApplicationService online");
        } catch (RemoteException e) {
            //e.printStackTrace();
            System.out.println("problemen met server");
        }


        return applicationServer;
    }

    //zet de effectieve applicatieserver uit
    @Override
    public void stopApplicationServer(int port) throws RemoteException {
        try {
            if (onlinePorts.contains(port)) {
                registry = appserverMap.get(port);
                System.out.println("shutting down applicationserver");
                registry.unbind("applicationService");
                appserverMap.remove(port);
            }
        } catch (NotBoundException e) {
            //e.printStackTrace();
            System.out.println("allready unbound");
        }

        applicationServer[0] = "";
        applicationServer[1] = "";
    }

    //wordt opgeroepen wanneer dispatcher een user wilt doorverbinden naar een applicatieserver
    //deze methode kunnen we later gebruiken om applicatieservers bij te maken wanneer er iemand bij komt --> wanneer spel bijkomt
    @Override
    public String[] addUser(int port, int databasePort) throws RemoteException {
        System.out.println("entering addUser");


        startApplicationServer(port,databasePort);

        userCount++;
        System.out.println(userCount);
        return applicationServer;
    }

    //wordt opgeroepen wanneer een user de applicatieserver verlaat
    //deze methode kunnen we later gebruiken om applicatieservers te verwijderen wanneer er iemand weg gaat
    @Override
    public void removeUser() throws RemoteException {
        System.out.println("user left multipleApp");
        userCount--;
        System.out.println(userCount);


    }
}
