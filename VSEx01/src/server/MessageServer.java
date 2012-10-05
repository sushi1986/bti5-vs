package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.HashMap;
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

    public MessageServer(int maxMessages, long clientTimeout) {
        super();
        this.messageQueue = new HashMap<Long, Message>(maxMessages);
        this.clients = new HashMap<String, ClientData>();
        this.clientTimeout = clientTimeout;
        messageIdGen = new IdGenerator();
    }

    @Override
    public String nextMessage(String clientID) throws RemoteException {
        ClientData tmp = clients.get(clientID);
        if(tmp==null) {
           SortedSet<Long> messageIds = new TreeSet<Long>(messageQueue.keySet());
           tmp = new ClientData(clientID, messageIds.first(), new Date().getTime() + clientTimeout);
           clients.put(clientID, tmp);
        }
        Message returnMessage = messageQueue.get(tmp.getMessageId());
        if (returnMessage != null) {
            tmp.setMessageId(tmp.getMessageId()+1);
        }
        return returnMessage.toString();
    }

    @Override
    public void addMessage(String clientID, String message) throws RemoteException {
        Message tmp = new Message(messageIdGen.nextId(), clientID, message);
        messageQueue.put(tmp.messageId, tmp);
    }
}
