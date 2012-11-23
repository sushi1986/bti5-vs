package mware_lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Communicator extends NameService implements Runnable {

    private static final boolean DEBUG = false;

    private Map<String, Object> bound;
    private Map<String, Info> resolved;
    private volatile boolean running;
    private ServerSocket srvSck;

    private String host;
    private int port;
    private int listenPort;

    public Communicator(int localPort, String srvHost, int srvPort) {
        super();
        running = true;
        try {
            srvSck = new ServerSocket(localPort);
        } catch (IOException e) {
            if (DEBUG) System.err.println("[!!!] Could not open server socker on '" + localPort +"'.");
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
            if (DEBUG)
                System.out.print("[DBG] " + putMessage);
            out.write(putMessage.getBytes());
            String message = in.readLine();
            if (DEBUG)
                System.out.println("[DBG] Answer to rebind '" + name + "': '" + message + "'");
            bound.put(name, servant);
        } catch (Exception e) {
            if (DEBUG) System.err.println("[!!!] Rebind failed: '" + e.getLocalizedMessage() + "'.");
        }
    }

    @Override
    public Object resolve(String name) {
        try {
            Socket sck = new Socket(host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
            OutputStream out = sck.getOutputStream();
            String getMessage = "get::" + name + "\n";
            if (DEBUG)
                System.out.println("[DBG] " + getMessage);
            out.write(getMessage.getBytes());
            String message = in.readLine();
            if (DEBUG)
                System.out.println("[DBG] Answer to resolve '" + name + "': '" + message + "'");
            String[] parts = message.split("::");
            if (parts[0].equals("exc")) {
                return null;
            } else if (!parts[0].equals("ack") || !parts[1].equals("get") || parts.length < 3) {
                if (DEBUG)
                    System.err.println("[!!!] Could not resolve '" + name + "', broker answer was invalid.");
                return null;
            } else {
                String absClass = parts[2];
                String host = parts[3];
                int port = Integer.valueOf(parts[4]);
                resolved.put(name, new Info(name, host, port));
                Class<?>[] argTypes = new Class<?>[] { String.class, String.class, int.class };
                Object obj = null;
                obj = Class.forName(absClass + "Remote").getConstructor(argTypes).newInstance(name, host, port);
                return obj;
            }
        } catch (Exception exc) {
            if (DEBUG)
                System.err.println("[!!!] Could not resolve: '" + name + "'.");
            return null;
        }
    }

    public String callOnResolved(String name, String method, Object... args) {
        String message = null;
        try {
            Info info = resolved.get(name);
            Socket sck = new Socket(info.getHost(), info.getPort());
            BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
            OutputStream out = sck.getOutputStream();
            String call = "call::" + name + "::" + method;
            for (Object arg : args) {
                call = call.concat("::" + arg.getClass().getName() + ":" + arg.toString());
            }
            call = call.concat("\n");
            if (DEBUG)
                System.out.print("[DBG] " + call);
            out.write((call).getBytes());
            message = in.readLine();
        } catch (IOException e) {
            return "exc::" + e.getClass().getName() + "::" + e.getMessage();
        }
        String[] parts = message.split("::");
        if (!parts[0].equals("return")) {
            return null;
        } else {
            if (parts[3].equals("exc")) {
                String excName = parts[4];
                String excArgument = parts[5];
                return "exc::" + excName + "::" + excArgument;
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
                final Socket sck = srvSck.accept();
                Thread para = new Thread() {
                    @Override
                    public void run() {
                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
                            String message = in.readLine();
                            String[] parts = message.split("::");
                            String name = parts[1];
                            String method = parts[2];
                            Object obj = bound.get(name);
                            Class<?> objClass = obj.getClass();
                            Class<?>[] argTypes = new Class<?>[parts.length - 3];
                            Object[] args = new Object[parts.length - 3];
                            for (int j = 0; j < argTypes.length; j++) {
                                String[] arg = parts[j + 3].split(":");
                                String type = arg[0];
                                String value = arg[1];
                                try {
                                    Class<?> tmp = Class.forName(type);
                                    if (tmp.getName().contains("Double")) {
                                        argTypes[j] = double.class;
                                    } else {
                                        argTypes[j] = tmp;
                                    }
                                    args[j] = tmp.getConstructor(String.class).newInstance(value);
                                } catch (Exception e) {
                                    if (DEBUG)
                                        System.err.println("[!!!] Problem obtaining arguments for call '" + method
                                                + "' on '" + name + "'.");
                                }
                            }
                            String result = null;
                            Object ret = null;
                            try {
                                ret = objClass.getMethod(method, argTypes).invoke(obj, args);
                                if (ret == null) {
                                    result = "void";
                                } else {
                                    result = ret.toString();
                                }
                            } catch (InvocationTargetException e) {
                                result = "exc::" + e.getTargetException().getClass().getName() + "::"
                                        + e.getTargetException().getMessage();
                            } catch (Exception exc) {
                                if (DEBUG)
                                    System.err.println("[!!!] Exception with reflections (" + exc.getLocalizedMessage()
                                            + ").");
                                result = "exc::" + exc.getClass().getName() + "::" + exc.getMessage();
                            }
                            OutputStream out;
                            out = sck.getOutputStream();
                            String returnMessage = "return::" + name + "::" + method + "::" + result + "\n";
                            out.write(returnMessage.getBytes());
                        } catch (IOException e) {
                            if (DEBUG)
                                System.err.println("[!!!] Some problem with socket connection to '" + host + ":" + port
                                        + "'.");
                        } finally {
                            try {
                                sck.close();
                            } catch (IOException e) {
                                if (DEBUG)
                                    System.err.println("[!!!] Problem closing socket connection to '" + host + ":"
                                            + port + "'.");
                            }
                        }
                    }
                };
                para.start();
            } catch (IOException e) {
                if (DEBUG) System.err.println("[!!!] Accepting request failed: '" + e.getLocalizedMessage() + "'.");
            }
        }
    }
}
