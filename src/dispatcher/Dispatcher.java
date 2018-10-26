package dispatcher;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Dispatcher {

    public static void start(){
        try {
            Registry registry = LocateRegistry.createRegistry(1299);
            registry.rebind("dispatchService", new DispatchProtocolImpl());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("Dispatcher online");

    }

    public static void main(String[] args){
        start();
    }
}
