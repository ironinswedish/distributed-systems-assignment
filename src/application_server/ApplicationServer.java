package application_server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ApplicationServer {

    public static void start() {
        try {
            Registry registry = LocateRegistry.createRegistry(1399);

            //voor distributed communicatie
            registry.rebind("multipleAppService", new MultipleAppProtocolImpl());

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("ApplicationServer online");
    }

    public static void main(String[] args){
        start();
    }
}
