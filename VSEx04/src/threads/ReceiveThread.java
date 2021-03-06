package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

/**
 * VS Lab4
 * @author Phillip Gesien, Raphael Hiesgen
 */

public class ReceiveThread extends Thread {

    BlockingQueue<Message> receivedMsgs;
    MulticastSocket mSck;

    public ReceiveThread(BlockingQueue<Message> rcvMsgs, String group, int port) {
        this.receivedMsgs = rcvMsgs;
        try {
            mSck = new MulticastSocket(port);
            mSck.joinGroup(InetAddress.getByName(group));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        System.out.println("[RT] Receive thread running.");
        byte[] buffer = new byte[1024];
        while (!isInterrupted()) {
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            try {
                mSck.receive(dp);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            Message m = new Message(Arrays.copyOfRange(buffer, 0, 33));
            try {
                receivedMsgs.put(m);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mSck.close();
    }

    public BlockingQueue<Message> getReceivedMsgs() {
        return receivedMsgs;
    }

}
