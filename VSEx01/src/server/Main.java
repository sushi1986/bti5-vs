package server;

import interfaces.IMessageServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {
    
    final static int MAX_MESSAGES = 10;
    final static long CLIENT_TIMEOUT_SECS = 30; 
    
    public static void main(String[] args) {

        try {
            String name = "Message Server";
            IMessageServer server = new MessageServer(MAX_MESSAGES, CLIENT_TIMEOUT_SECS);
            IMessageServer stub = (IMessageServer) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Message Server bound");
        } catch (Exception e) {
            System.err.println("Message Server exception:");
            e.printStackTrace();
        }

//        while (true) {
//            try {
//                Thread.sleep(3);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
