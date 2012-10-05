package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Queue;

import interfaces.IMessageServer;

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
	public void addMessage(String clientID, String message) throws RemoteException {
		
		messageQueue.add(clientID + ": "+message);
	}

	public static void main(String[] args) {
		try {
			String name = "Message Server";
			IMessageServer server = new MessageServer();
			IMessageServer stub = (IMessageServer) UnicastRemoteObject
					.exportObject(server, 0);
			Registry registry = LocateRegistry.getRegistry();
			registry.rebind(name, stub);
			System.out.println("Message Server bound");
		} catch (Exception e) {
			System.err.println("Message Server exception:");
			e.printStackTrace();
		}
		
		while(true){
			try {
				Thread.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
