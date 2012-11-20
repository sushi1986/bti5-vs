package mware_lib;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectBroker {

	private static ObjectBroker broker;
	private static Lock mutex = new ReentrantLock();
//	private String host;
//	private int port;
	private Thread thread;
	private NameServiceImpl nsi;

	final static int PORT = 2555;

	public ObjectBroker(String host, int port) {
		super();
//		this.host = host;
//		this.port = port;
		nsi = new NameServiceImpl(PORT, host, port);
		Thread thread = new Thread(nsi);
		this.thread = thread;
		thread.start();
	}

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

	public static ObjectBroker getBroker(String serviceHost, int listenPort) {
		mutex.lock();
		if (broker == null) {
			broker = new ObjectBroker(serviceHost, listenPort);
		}
		mutex.unlock();
		return broker;
	}
}
