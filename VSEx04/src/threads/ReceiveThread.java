package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReceiveThread extends Thread {

	BlockingQueue<Message> receivedMsgs;

	MulticastSocket mSck;

	public ReceiveThread(String group, int port) {
		receivedMsgs = new LinkedBlockingQueue<Message>();

		try {
			mSck = new MulticastSocket(port);
			mSck.joinGroup(InetAddress.getByName(group));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		System.out.println("[RT]Receive Thread now running.");
		byte[] buffer = new byte[1024];

		while (!isInterrupted()) {

			// receive
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			try {
				mSck.receive(dp);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Message m = new Message(Arrays.copyOfRange(buffer, 0, 32));

			System.out.print("[RT]Received packet:\nfrom: " + dp.getAddress()
					+ ":" + dp.getPort() + "\ncontains: ");
			System.out.println(m);
			
			try {
				receivedMsgs.put(m);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		mSck.close();
	}

	public BlockingQueue<Message> getReceivedMsgs() {
		return receivedMsgs;
	}
	
}
