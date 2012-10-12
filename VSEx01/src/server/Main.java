package server;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

import aufgabe1.server.MessageServer;

/**
 * VS Lab 1
 * HAW Hamburg
 * 
 * @author Phillip Gesin, Raphael Hiesgen
 */

public class Main {
    
    final static int MAX_MESSAGES = 10;
    final static long CLIENT_TIMEOUT_MSECS = 5000; 
    
    public static void main(String[] args) {
    	MessageServer server = new MessageServerImpl(MAX_MESSAGES, CLIENT_TIMEOUT_MSECS);
        try {
            String name = "MessageServer";
            MessageServer stub = (MessageServer) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Message Server bound");
        } catch (Exception e) {
            System.err.println("Message Server exception:");
            e.printStackTrace();
        }
    }
}
