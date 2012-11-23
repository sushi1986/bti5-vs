package mware_lib;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObjectBroker {

    private static final boolean DEBUG = false;

    private final int MAX_PORT = 65536;

    private static ObjectBroker broker;
    private static Lock mutex = new ReentrantLock();

    private Thread thread;
    private Communicator nsi;

    public ObjectBroker(String host, int port) {
        super();
        int randomPort = new Random().nextInt(MAX_PORT - 1025) + 1025;
        nsi = new Communicator(randomPort, host, port);
        Thread thread = new Thread(nsi);
        this.thread = thread;
        thread.setDaemon(true);
        thread.start();
    }

    public NameService getNameService() {
        return nsi;
    }

    public void join() {
        try {
            thread.join();
        } catch (InterruptedException e) {
            if (DEBUG)
                System.err.println("[!!!] Could not join communication thread.");
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
