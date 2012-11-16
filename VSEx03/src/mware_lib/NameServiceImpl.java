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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nameservice.Info;

public class NameServiceImpl extends NameService implements Runnable {

	private Map<String, Object> bound;
	private Lock objLock;
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
		objLock = new ReentrantLock();
	}

	@Override
	public void rebind(Object servant, String name) {
		try {
			Socket sck = new Socket(host, port);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					sck.getInputStream()));
			OutputStream out = sck.getOutputStream();
			System.out.println("put::" + name + "::"
					+ sck.getLocalAddress().getHostAddress() + "::"
					+ listenPort);
			out.write(("put::" + name + "::"
					+ sck.getLocalAddress().getHostAddress() + "::"
					+ listenPort + "\n").getBytes());
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
			BufferedReader in = new BufferedReader(new InputStreamReader(
					sck.getInputStream()));
			OutputStream out = sck.getOutputStream();
			System.out.print("get::" + name + "\n");
			out.write(("get::" + name + "\n").getBytes());
			String message = in.readLine();
			System.out.println("[DBG] Answer to resolve: '" + message + "'");
			String[] parts = message.split("::");
			;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * call::NAME::METHOD::arg0::arg1...
	 * return::NAME::METHOD::VALUE
	 */
	public Object callOnResolved(String name, String host, int port, String method) {
		try {
			Socket sck = new Socket(host, port);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					sck.getInputStream()));
			OutputStream out = sck.getOutputStream();
			String call = "call::"+name+"::"+method+"\n";
			System.out.print(call);
			out.write((call).getBytes());
			String message = in.readLine();
			System.out.println("[DBG] Answer to call: '" + message + "'");
			String[] parts = message.split("::");
			;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void run() {
		System.out.println("NameService is now running on port: " + srvSck.getLocalPort());
		while (running) {
			try {
				Socket sck = srvSck.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						sck.getInputStream()));
				String message = in.readLine();
				System.out.println("[DBG] Received in run: '" + message + "'.");
				String[] parts = message.split("::");
				String name = parts[1];
				String method = parts[2];
				Object obj = bound.get(name);
				String rc = null;
				try {
					rc = (String) obj.getClass().getMethod(parts[2]).invoke(obj);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
				OutputStream out = sck.getOutputStream();
				String returnMessage = "return::"+name+"::"+method+"::"+rc+"\n";
				out.write(returnMessage.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
