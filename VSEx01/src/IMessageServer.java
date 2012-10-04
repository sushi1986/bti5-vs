
import java.rmi.*;

public interface IMessageServer extends Remote {
	public String nextMessage(String clientID) throws RemoteException;

	public void addMessage(String clientID, String message)
			throws RemoteException;
}