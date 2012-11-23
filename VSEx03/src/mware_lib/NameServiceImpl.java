package mware_lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

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
			BufferedReader in = new BufferedReader(new InputStreamReader(
					sck.getInputStream()));
			OutputStream out = sck.getOutputStream();
			String superClass = servant.getClass().getSuperclass().getName();
			String putMessage = "put::" + name + "::" + superClass + "::"
					+ sck.getLocalAddress().toString() + "::" + listenPort
					+ "\n";
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
			BufferedReader in = new BufferedReader(new InputStreamReader(
					sck.getInputStream()));
			OutputStream out = sck.getOutputStream();
			String getMessage = "get::" + name + "\n";
			System.out.print(getMessage);
			out.write(getMessage.getBytes());
			String message = in.readLine();
			System.out.println("[DBG] Answer to resolve: '" + message + "'");
			String[] parts = message.split("::");
			if (parts[0].equals("exc")) {
				return null;
			} else if (!parts[0].equals("ack") || !parts[1].equals("get")
					|| parts.length < 3) {
				System.out.println("[DBG] Resolve answer was wrong ...");
				return null;
			} else {
				String absClass = parts[2];
				String host = parts[3];
				int port = Integer.valueOf(parts[4]);
				resolved.put(name, new Info(name, host, port));
				Class<?>[] argTypes = new Class<?>[] { String.class,
						String.class, int.class };
				Object obj = null;
				obj = Class.forName(absClass + "Remote")
						.getConstructor(argTypes).newInstance(name, host, port);
				return obj;
			}
		} catch (Exception exc) {
			System.out.println("[DBG][Resolve] : caught them all! "
					+ exc.getMessage());
			return null;
		}
	}

	/*
	 * call::NAME::METHOD::arg0::arg1... return::NAME::METHOD::VALUE
	 */
	public String callOnResolved(String name, String method, Object... args) {
		String message = null;
		try {
			Info info = resolved.get(name);
			Socket sck = new Socket(info.getHost(), info.getPort());
			BufferedReader in = new BufferedReader(new InputStreamReader(
					sck.getInputStream()));
			OutputStream out = sck.getOutputStream();
			String call = "call::" + name + "::" + method;
			for (Object arg : args) {
				call = call.concat("::" + arg.getClass().getName() + ":" + arg.toString());
			}
			call = call.concat("\n");
			System.out.print(call);
			out.write((call).getBytes());
			message = in.readLine();
		} catch (IOException e) {
			return "exc::" + e.getClass().getName() + "::" + e.getMessage();
		}
		System.out.println("[DBG] Answer to call: '" + message + "'");
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
		System.out.println("NameService is now running on port: "
				+ srvSck.getLocalPort());
		while (running) {
			try {
				final Socket sck = srvSck.accept();

				Thread para = new Thread() {
					@Override
					public void run() {
						try {
							BufferedReader in = new BufferedReader(
									new InputStreamReader(sck.getInputStream()));
							String message = in.readLine();
							System.out.println("[DBG] Received in run: '"
									+ message + "'.");
							String[] parts = message.split("::");
							String name = parts[1];
							String method = parts[2];
							Object obj = bound.get(name);
							String result = null;

							Class<? extends Object> c = obj.getClass();

							Class<? extends Object>[] actualArgumentClasses = new Class<?>[parts.length - 3];
							Object[] actualArguments = new Object[parts.length - 3];

							for (int j = 0; j < actualArgumentClasses.length; j++) {
								String[] p = parts[j + 3].split(":");
								String typ = p[0];
								String parameter = p[1];

								try {
									Class<?> klasse = Class.forName(typ);
									actualArgumentClasses[j] = klasse;

									actualArguments[j] = klasse
											.getConstructor(String.class)
											.newInstance(parameter);
									
									if(klasse.getName().contains("Double")){
										actualArgumentClasses[j] = double.class;
									}
									
								} catch (ClassNotFoundException e) {
									e.printStackTrace();
								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								} catch (SecurityException e) {
									e.printStackTrace();
								} catch (InstantiationException e) {
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									e.printStackTrace();
								} catch (NoSuchMethodException e) {
									e.printStackTrace();
								}
							}

							Method calledM = null;
							try {
								calledM = c.getMethod(method,
										actualArgumentClasses);
							} catch (SecurityException e1) {
								e1.printStackTrace();
							} catch (NoSuchMethodException e1) {
								e1.printStackTrace();
							}

							if (calledM != null) {
								Object ret = null;
								

								try {
									ret = calledM.invoke(obj, actualArguments);
									if (ret == null) {
										result = "void";
									} else {
										result = ret.toString();
									}
								} catch (InvocationTargetException e) {
									result = "exc::"
											+ e.getTargetException().getClass()
													.getName()
											+ "::"
											+ e.getTargetException()
													.getMessage();
								} catch (Exception exc) {
									exc.printStackTrace();
									result = "exc::" + exc.getClass().getName()
											+ "::" + exc.getMessage();
								}
							}
							OutputStream out;
							out = sck.getOutputStream();

							String returnMessage = "return::" + name + "::"
									+ method + "::" + result + "\n";
							out.write(returnMessage.getBytes());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				para.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
