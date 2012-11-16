package mware_lib;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectBroker {
	
	private static ObjectBroker broker;
	private static Lock mutex = new ReentrantLock();
	private String host;
	private int port;
	
	public ObjectBroker(String host, int port) {
		super();
		this.host = host;
		this.port = port;
		
	}

	// Liefert den Namensdienst (Stellvetreterobjekt).
	public NameService getNameService() {
		return new NameServiceImpl(2552, host, port);
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
