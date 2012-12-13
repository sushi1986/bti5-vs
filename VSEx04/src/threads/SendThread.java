package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

/**
 * VS Lab4
 * @author Phillip Gesien, Raphael Hiesgen
 */

public class SendThread extends Thread {
    private BlockingQueue<Message> msgsToSend;
    private MulticastSocket mSck;

    private String group;
    private int port;

    public SendThread(BlockingQueue<Message> sndMsgs, String group, int port) {
        this.msgsToSend = sndMsgs;
        try {
            mSck = new MulticastSocket();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.group = group;
        this.port = port;
    }

    @Override
    public void run() {
        System.out.println("[RT] Send thread running.");
        Message m = null;
        while (!isInterrupted()) {
            try {
                m = msgsToSend.take();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (m == null) {
                System.out.println("[ST] Error take = null");
            }
            else {
                byte[] buffer = m.getBytes();
                DatagramPacket dp = null;
                try {
                    dp = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(group), port);
                    mSck.send(dp);
                }
                catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public BlockingQueue<Message> getMsgsToSend() {
        return msgsToSend;
    }
}
