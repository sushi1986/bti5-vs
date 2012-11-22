package mware_lib;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectBroker {

    private final int MAX_PORT = 65536;
    
	private static ObjectBroker broker;
	private static Lock mutex = new ReentrantLock();
	
	private Thread thread;
	private NameServiceImpl nsi;

	public ObjectBroker(String host, int port) {
		super();
		short randomPort = (short) (new Random().nextInt(MAX_PORT-1025)+1025);
		nsi = new NameServiceImpl(randomPort, host, port);
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
