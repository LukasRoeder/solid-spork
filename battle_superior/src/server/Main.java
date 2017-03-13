package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import static utils.SupUtils.*;

public class Main {
	public static void main(String[] args) throws RemoteException{
 
		 try {
			LocateRegistry.createRegistry(Registry.REGISTRY_PORT).rebind(RMI_NAME, new Server());
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}
}
