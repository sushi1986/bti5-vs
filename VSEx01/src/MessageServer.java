

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;


public class MessageServer implements IMessageServer {

	Queue<String> messageQueue;

	public MessageServer() {
		super();
		messageQueue = new LinkedList<String>();
	}

	@Override
	public String nextMessage(String clientID) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addMessage(String clientID, String message)
			throws RemoteException {
		messageQueue.add(clientID + ": "+message);
	}

	public static void main(String[] args) {
//		System.setSecurityManager (new RMISecurityManager() {
//		    public void checkConnect (String host, int port) {}
//		    public void checkConnect (String host, int port, Object context) {}
//		  });
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		try {
			String name = "Compute";
			IMessageServer engine = new MessageServer();
			IMessageServer stub = (IMessageServer) UnicastRemoteObject
					.exportObject(engine, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(name, stub);
			System.out.println("ComputeEngine bound");
		} catch (Exception e) {
			System.err.println("ComputeEngine exception:");
			e.printStackTrace();
		}
	}

}
