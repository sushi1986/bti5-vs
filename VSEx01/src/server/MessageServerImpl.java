package server;

import aufgabe1.server.MessageServer;

import java.rmi.RemoteException;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * VS Lab 1
 * HAW Hamburg
 * 
 * @author Phillip Gesin, Raphael Hiesgen
 * 
 * Server implementation.
 */

public class MessageServerImpl implements MessageServer {
    
    Map<Long, Message> messageQueue;
    Map<String, ClientData> clients;
    IdGenerator messageIdGen;
    long clientTimeout;
    long currentMinId;
    int maxMessages;

    public MessageServerImpl(int maxMessages, long clientTimeout) {
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
