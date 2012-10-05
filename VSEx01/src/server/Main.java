package server;

import interfaces.IMessageServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {
    
    final static int MAX_MESSAGES = 10;
    final static long CLIENT_TIMEOUT_MSECS = 5000; 
    
    public static void main(String[] args) {
    	IMessageServer server = new MessageServer(MAX_MESSAGES, CLIENT_TIMEOUT_MSECS);
        try {
            String name = "Message Server";
            IMessageServer stub = (IMessageServer) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Message Server bound");
        } catch (Exception e) {
            System.err.println("Message Server exception:");
            e.printStackTrace();
        }
        
//        do {
//        	try {
//				Thread.sleep(1);
//				((MessageServer) server).removeTimedOutClients();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//        	
//        } while (true);

//        while (true) {
//            try {
//                Thread.sleep(3);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
