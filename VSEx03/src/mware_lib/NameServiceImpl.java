package mware_lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import nameserver.Info;
import branch_access.Manager;
import cash_access.Account;

public class NameServiceImpl extends NameService implements Runnable {

    private Map<String, Object> bound;
    private Map<String, Info> resolved;
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
        resolved = new HashMap<String, Info>();
    }

    @Override
    public void rebind(Object servant, String name) {
        try {
            Socket sck = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
            OutputStream out = sck.getOutputStream();
            String superClass = servant.getClass().getSuperclass().getName();
            String putMessage = "put::" + name + "::" + superClass + "::" + sck.getLocalAddress().toString() + "::"
                    + listenPort + "\n";
            System.out.print(putMessage);
            out.write(putMessage.getBytes());
            String message = in.readLine();
            System.out.println("[DBG] Answer to rebind: '" + message + "'");
            bound.put(name, servant);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object resolve(String name) {
        try {
            Socket sck = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
            OutputStream out = sck.getOutputStream();
            String getMessage = "get::" + name + "\n";
            System.out.print(getMessage);
            out.write(getMessage.getBytes());
            String message = in.readLine();
            System.out.println("[DBG] Answer to resolve: '" + message + "'");
            String[] parts = message.split("::");
            if (parts[0].equals("exc")) {
                return null;
            } else if (!parts[0].equals("ack") || !parts[1].equals("get") || parts.length < 3) {
                System.out.println("[DBG] Resolve answer was wrong ...");
                return null;
            } else {
                String absClass = parts[2]; 
                String host = parts[3];
                int port = Integer.valueOf(parts[4]);
                resolved.put(name, new Info(name, null, host, port));
                Class<?>[] argTypes= new Class<?>[]{String.class, String.class, Integer.class};
                Object obj = null;
                obj = Class.forName(absClass+"Remote").getConstructor(argTypes).newInstance(name, host, port);
                return obj;
            }
        } catch (Exception exc) {
            System.out.println("[DBG] Resolve, caught them all!");
            return null;
        }
    }

    /*
     * call::NAME::METHOD::arg0::arg1... return::NAME::METHOD::VALUE
     */
    public String callOnResolved(String name, String method, String... args) throws Exception {
        Info info = resolved.get(name);
        Socket sck = new Socket(info.getHost(), info.getPort());
        BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
        OutputStream out = sck.getOutputStream();
        String call = "call::" + name + "::" + method;
        for (String arg : args) {
            call = call.concat("::" + arg);
        }
        call = call.concat("\n");
        System.out.print(call);
        out.write((call).getBytes());
        String message = in.readLine();
        System.out.println("[DBG] Answer to call: '" + message + "'");
        String[] parts = message.split("::");
        if (!parts[0].equals("return")) {
            return null;
        } else {
            if (parts[3].equals("exc")) {
                String excName = parts[4];
                String excArgument = (parts[5].equals("null") ? null : parts[5]);
                System.out.println("[!!!] throwing exception: '"+excName+"' with argument '"+excArgument+"'.");
                Exception exc = null;
                exc = (Exception) Class.forName(excName).getConstructor(new Class<?>[]{String.class}).newInstance(excArgument);
                throw exc;
            } else {
                return parts[3];
            }
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
                String result = null;
                if (method.equals("deposit")) {
                    try {
                        ((Account) obj).deposit(Double.valueOf(parts[3]));
                        result = "void";
                    } catch (Exception exc) {
                        result = "exc::" + exc.getClass().getName() + "::" + exc.getMessage();
                    }
                } else if (method.equals("withdraw")) {
                    try {
                        ((Account) obj).withdraw(Double.valueOf(parts[3]));
                        result = "void";
                    } catch (Exception exc) {
                        result = "exc::" + exc.getClass().getName() + "::" + exc.getMessage();
                    }
                } else if (method.equals("getBalance") && parts.length == 3) {
                    result = String.valueOf(((Account) obj).getBalance());
                } else if (method.equals("createAccount")) {
                    result = ((Manager) obj).createAccount(parts[3]);
                } else if (method.equals("getBalance")) {
                    result = String.valueOf(((Manager) obj).getBalance(parts[3]));
                }
                OutputStream out = sck.getOutputStream();
                String returnMessage = "return::" + name + "::" + method + "::" + result + "\n";
                out.write(returnMessage.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
