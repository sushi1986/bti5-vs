package client.controller;

import aufgabe1.server.MessageServer;

import java.util.Date;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.NetworkInterface;
import java.net.UnknownHostException;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

import client.view.*;

/**
 * VS Lab 1 HAW Hamburg
 * 
 * @author Phillip Gesin, Raphael Hiesgen
 * 
 * Implements communication between GUI and server.
 * 
 */

public class Controller {

    private final String REMOTE_OBJ_NAME = "MessageServer";
    private final long RECONNT_TIMEOUT = 10000;
    private View view;
    private MessageServer msgServer;
    private long id;
    private String lastConnected;

    public Controller(View view) {
        super();
        this.view = view;
        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "3000");
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            System.out.println("Current IP address : " + ip.getHostAddress());
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            System.out.print("Current MAC address : ");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            System.out.println(sb.toString());
            id = ip.getHostAddress().hashCode() + sb.toString().hashCode() + new Date().hashCode();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        System.out.println("Client ID: " + id);
    }

    /**
     * connects to a server
     * 
     * @param address
     *            the server adress
     */
    public boolean connect(String address) {
        boolean rc = true;
        try {
            Registry registry = LocateRegistry.getRegistry(address);
            msgServer = (MessageServer) registry.lookup(REMOTE_OBJ_NAME);
            try{
                msgServer.nextMessage("FOO IS TESTING THE CONNECTION .... ");
                updateTextview("Connected to Server.");
            } catch (RemoteException exc) {
                rc = false;
            }
            lastConnected = address;
        } catch (Exception exc) {
            rc = false;
        }
        return rc;
    }

    /**
     * send a message to the server
     * 
     * @param msg
     *            message to send
     */
    public void boring_send(String msg) {
        if (msg == null || msg.replace(" ", "").equals("")) {
            return;
        }

        /*
         * at least once -> try to reconnect if connection fails
         */
        boolean isSent = false;
        long timeout = new Date().getTime() + RECONNT_TIMEOUT;
        do {
            try {
                msgServer.addMessage("" + id, msg);
                isSent = true;
            } catch (RemoteException exc) {
                connect(lastConnected);
            }
        } while (!isSent && new Date().getTime() <= timeout);

        if (isSent) {
            String sentMsg = "Sent message \"" + msg + "\".";
            System.out.println(sentMsg);
            updateTextview(sentMsg);
        } else {
            String errMsg = "Could not send message to server, connection timed out. (\"" + msg + "\")";
            System.err.println(errMsg);
            updateTextview(errMsg);
        }
    }
    
    public void send(String msg) {
        if (msg == null || msg.replace(" ", "").equals("")) {
            return;
        }

        /*
         * at least once -> try to reconnect if connection fails
         */
        boolean done = false;
        do {
            try {
                msgServer.addMessage("" + id, msg);
                done = true;
            } catch (RemoteException exc) {
                String discoMsg = "Disconnected from server, trying to reconnect ...";
                System.out.println(discoMsg);
                updateTextview(discoMsg);
                long timeout = new Date().getTime() + RECONNT_TIMEOUT;
                do {
                    done = !connect(lastConnected);
                } while (new Date().getTime() <= timeout && done);
                if(!done) {
                    String recoMsg = "Reconnect, trying to resend msg.";
                    System.out.println(recoMsg);
                    updateTextview(recoMsg);
                } else {
                    String failedMsg = "Reconnect failed ...";
                    System.out.println(failedMsg);
                    updateTextview(failedMsg);
                }
            }
        } while (!done);
    }

    public void updateTextview(String msg) {
        view.getTxtrLeser().setText(view.getTxtrLeser().getText() + "\n" + msg);
    }

    /**
     * asks a server for a new message
     * 
     * @return false if there is no message
     */
    public boolean receive() {
        try {
            String clientID = "" + id;
            String receive = msgServer.nextMessage(clientID);
            System.out.println("Received: " + receive);
            if (receive == null) {
                return false;
            } else {
                view.getTxtrLeser().setText(view.getTxtrLeser().getText() + "\n" + receive);
            }
        } catch (RemoteException e) {
            return false;
        }
        return true;

    }

    /**
     * retrieves all available messages from server
     */
    public void receiveAll() {
        System.out.println("Receive all");
        while (receive())
            ;
    }

    @Override
    public String toString() {
        return "Controller [msgServer=" + msgServer + ", id=" + id + "]";
    }
}
