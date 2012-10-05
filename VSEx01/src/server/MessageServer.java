package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import interfaces.IMessageServer;

public class MessageServer implements IMessageServer {
    
    Map<Long, Message> messageQueue;
    Map<String, ClientData> clients;
    IdGenerator messageIdGen;
    long clientTimeout;
    long currentMinId;
    int maxMessages;

    public MessageServer(int maxMessages, long clientTimeout) {
        super();
        this.maxMessages = maxMessages;
        this.messageQueue = new HashMap<Long, Message>(maxMessages);
        this.clients = new HashMap<String, ClientData>();
        this.clientTimeout = clientTimeout;
        this.messageIdGen = new IdGenerator();
    }

    @Override
    public String nextMessage(String clientID) throws RemoteException {
    	removeTimedOutClients();
        ClientData tmp = clients.get(clientID);
        if(tmp==null) {
           tmp = new ClientData(clientID, currentMinId, clientTimeout);
           clients.put(clientID, tmp);
        } else {
        	tmp.setTimeout(clientTimeout);
        }
        if(tmp.getMessageId() < currentMinId) {
        	tmp.setMessageId(currentMinId);
        }
        Message returnMessage = messageQueue.get(tmp.getMessageId());
        if (returnMessage != null) {
            tmp.setMessageId(tmp.getMessageId()+1);
            return returnMessage.toString();
        }
        return null;
    }

    @Override
    public void addMessage(String clientID, String message) throws RemoteException {
        Message tmp = new Message(messageIdGen.nextId(), clientID, message);
        if(messageQueue.size() == 0) {
        	currentMinId = tmp.getMessageId();
        } else if (messageQueue.size() == maxMessages) {
        	messageQueue.remove(currentMinId);
        	++currentMinId;
        }
        messageQueue.put(tmp.messageId, tmp);
    }
    
    public void removeTimedOutClients() {
    	long currentTime = new Date().getTime();
    	for (Iterator<String> itr = clients.keySet().iterator(); itr.hasNext();) {
			String key = (String) itr.next();
			if(clients.get(key).getTimeout() < currentTime) {
				itr.remove();
			}
		}
    }
}
