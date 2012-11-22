package mware_lib;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectBroker {

	private static ObjectBroker broker;
	private static Lock mutex = new ReentrantLock();

	private Thread thread;
	private NameServiceImpl nsi;


	public ObjectBroker(String host, int port) {
		super();
		
		short randomPort = (short) (new Random().nextInt(64511/*Wegen 2^16 Ports minus 1024 belegter*/)+1025);
		
		nsi = new NameServiceImpl(randomPort, host, port);
		Thread thread = new Thread(nsi);
		this.thread = thread;
		thread.start();
	}

	// Liefert den Namensdienst (Stellvetreterobjekt).
	public NameService getNameService() {
		return nsi;
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
		if (broker == null) {
			broker = new ObjectBroker(serviceHost, listenPort);
		}
		mutex.unlock();
		return broker;
	}
}
