package mware_lib;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectBroker {
	
	private static ObjectBroker broker;
	private static Lock mutex = new ReentrantLock();
	private String host;
	private int port;
	private Thread thread;
	
	public ObjectBroker(String host, int port) {
		super();
		this.host = host;
		this.port = port;
	}

	static int portX =2555;
	// Liefert den Namensdienst (Stellvetreterobjekt).
	public NameService getNameService() {
		NameServiceImpl tmp = new NameServiceImpl(portX++, host, port);
		Thread thread = new Thread(tmp);
		this.thread = thread;
		thread.start();
		return tmp;
	}
	
	public void join() {
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Das hier zurückgelieferte Objekt soll der zentrale Einstiegspunkt
	// der Middleware aus Anwendersicht sein.
	// Parameter: Host und Port, bei dem die Dienste (Namensdienst)
	// kontaktiert werden sollen.
	public static ObjectBroker getBroker(String serviceHost, int listenPort) {
		mutex.lock();
		if(broker == null) {
			broker = new ObjectBroker(serviceHost, listenPort);
		}
		mutex.unlock();
		return broker;
	}
}
