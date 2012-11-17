package mware_lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class NameServiceImpl extends NameService implements Runnable {

    private Map<String, Object> bound;
    private volatile boolean running;
    private ServerSocket srvSck;

    private String host;
    private int port;
    private int listenPort;

    public NameServiceImpl(int localPort, String srvHost, int srvPort) {
        super();
        running = true;
        try {
            srvSck = new ServerSocket(localPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.listenPort = localPort;
        this.host = srvHost;
        this.port = srvPort;
        bound = new HashMap<String, Object>();
    }

    @Override
    public void rebind(Object servant, String name) {
        try {
            Socket sck = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
            OutputStream out = sck.getOutputStream();
            System.out.println("put::" + name + "::" + sck.getLocalAddress().getHostAddress() + "::" + listenPort);
            out.write(("put::" + name + "::" + sck.getLocalAddress().getHostAddress() + "::" + listenPort + "\n")
                    .getBytes());
            String message = in.readLine();
            System.out.println("[DBG] Answer to rebind: '" + message + "'");
            bound.put(name, servant);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public Object resolve(String name) {
//        String[] parts = null;
//        try {
//            Socket sck = new Socket(host, port);
//            BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
//            OutputStream out = sck.getOutputStream();
//            System.out.print("get::" + name + "\n");
//            out.write(("get::" + name + "\n").getBytes());
//            String message = in.readLine();
//            System.out.println("[DBG] Answer to resolve: '" + message + "'");
//            parts = message.split("::");
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//            return null;
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//        if (parts[0].equals("exc")) {
//            //throw exception ?
//            return null;
//        } else if (!parts[0].equals("ack")) {
//            // throw exception
//            return null;
//        } else { // ack 
//            String newName = name.replace("Impl", "Remote");
//            Object obj = null;
//            try {
//                obj = Class.forName(newName).newInstance();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//            return obj;
//        }
//    }
    
    @Override
    public Object resolve(String name) {
        try {
            Socket sck = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
            OutputStream out = sck.getOutputStream();
            System.out.print("get::" + name + "\n");
            out.write(("get::" + name + "\n").getBytes());
            String message = in.readLine();
            System.out.println("[DBG] Answer to resolve: '" + message + "'");
            String[] parts = message.split("::");
            if (parts[0].equals("exc")) {
                return null;
            } else if (!parts[0].equals("ack")) {
                return null;
            } else { // ack 
                String newName = name.replace("Impl", "Remote");
                return Class.forName(newName).newInstance(); // argumente ...
            }
        } catch (Exception exc) {
            return null;
        }
        
    }

    /*
     * call::NAME::METHOD::arg0::arg1... return::NAME::METHOD::VALUE
     */
    public String callOnResolved(String name, String method, String... args) throws UnknownHostException,
            IOException {
        Socket sck = new Socket(host, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
        OutputStream out = sck.getOutputStream();
        String call = "call::" + name + "::" + method + "\n";
        System.out.print(call);
        out.write((call).getBytes());
        String message = in.readLine();
        System.out.println("[DBG] Answer to call: '" + message + "'");
        String[] parts = message.split("::");
        if (parts[0].equals("exc")) {
            return null;
        } else if (parts.length != 4) {
            return null;
        } else {
            return parts[3]; // type is always String ...
        }
    }

    @Override
    public void run() {
        System.out.println("NameService is now running on port: " + srvSck.getLocalPort());
        while (running) {
            try {
                Socket sck = srvSck.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
                String message = in.readLine();
                System.out.println("[DBG] Received in run: '" + message + "'.");
                String[] parts = message.split("::");
                String name = parts[1];
                String method = parts[2];
                Object obj = bound.get(name);
                String rc = null;
                String returnMessage = null;
                try {
                    rc = (String) obj.getClass().getMethod(parts[2]).invoke(obj);
                    returnMessage = "return::" + name + "::" + method + "::" + rc + "\n";
                } catch (IllegalArgumentException e) {
                    returnMessage = "exc::" + e.getClass().getName() + "::" + e.getMessage() + "\n";
                    e.printStackTrace();
                } catch (SecurityException e) {
                    returnMessage = "exc::" + e.getClass().getName() + "::" + e.getMessage() + "\n";
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    returnMessage = "exc::" + e.getClass().getName() + "::" + e.getMessage() + "\n";
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    returnMessage = "exc::" + e.getClass().getName() + "::" + e.getMessage() + "\n";
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    returnMessage = "exc::" + e.getClass().getName() + "::" + e.getMessage() + "\n";
                    e.printStackTrace();
                }
                OutputStream out = sck.getOutputStream();
                out.write(returnMessage.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
