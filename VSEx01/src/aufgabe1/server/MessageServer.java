package aufgabe1.server;

import java.rmi.*;

/**
 * VS Lab 1
 * HAW Hamburg
 * 
 * @author Phillip Gesien, Raphael Hiesgen
 */

public interface MessageServer extends Remote {
	public String nextMessage(String clientID) throws RemoteException;

	public void addMessage(String clientID, String message)
			throws RemoteException;
}