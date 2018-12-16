package application_server;

import Interfaces.MultipleAppProtocol;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/*
protocol verantwoordelijk voor communicatie tussen ApplicationServers
 */
public class
MultipleAppProtocolImpl extends UnicastRemoteObject implements MultipleAppProtocol {

    private boolean online = false;
    private int userCount;
    private Registry registry;
    private String[] applicationServer;

    protected MultipleAppProtocolImpl() throws RemoteException {

    }


    // kijken of server online is
    @Override
    public boolean getOnline() throws RemoteException {
        return online;
    }

    // startApplicationSevrever en stopApplicationServer zijn methodes voor het dynamisch bij creeren van appServers wanneer we gedistrubueerd gaan werken
    //deze methode start de effectieve applicatieServer op een andere poort
    //zet de host en het addres vast
    private String[] startApplicationServer() throws RemoteException {
        String port = "1350";
        String host = "localhost";
        applicationServer = new String[2];
        applicationServer[0] = "";
        applicationServer[1] = "";
        boolean bound = false;
        try {
            if (registry == null) {
                registry = LocateRegistry.createRegistry(Integer.parseInt(port));
            }
           /* String[] serviceList = registry.list();
            for (int i = 0; i < serviceList.length; i++) {
                if (serviceList[i].equals("applicationService")) {
                    bound = true;
                }
            }*/
            if (!online) {
                registry.rebind("applicationService", new ApplicationProtocolImpl());
            }


            applicationServer[0] = host;
            applicationServer[1] = port;

            online = true;
            System.out.println("ApplicationService online");
        } catch (RemoteException e) {
            //e.printStackTrace();
            System.out.println("problemen met server");
        }


        return applicationServer;
    }

    //zet de effectieve applicatieserver uit
    private void stopApplicationServer() throws RemoteException {
        try {
            if (registry != null) {
                System.out.println("shutting down applicationserver");
                registry.unbind("applicationService");
                online = false;
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
    public String[] addUser() throws RemoteException {
        System.out.println("entering addUser");


        startApplicationServer();

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
        if (userCount < 1) {
            stopApplicationServer();
        }
        System.out.println(userCount);


    }
}
