package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;

public class SendThread extends Thread {
	private BlockingQueue<Message> msgsToSend;
	private MulticastSocket mSck;

	private String group;
	private int port;

	public SendThread(BlockingQueue<Message> sndMsgs, String group, int port) {
		this.msgsToSend = sndMsgs;

		try {
			mSck = new MulticastSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.group = group;
		this.port = port;
	}

	@Override
	public void run() {
		Message m = null;
		while (!isInterrupted()) {
			try {
				m = msgsToSend.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (m == null) {
				System.out.println("[ST] Error take = null");
			} else {
				byte[] buffer = m.getBytes();
				DatagramPacket dp = null;
				try {
					dp = new DatagramPacket(buffer, buffer.length,
							InetAddress.getByName("225.10.1.2"), 15000);

					System.out.println("[ST] Now sending datagram with:\nto: "
							+ dp.getAddress() + ":" + dp.getPort()
							+ "\ncontains: " + m);
					mSck.send(dp);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public BlockingQueue<Message> getMsgsToSend() {
		return msgsToSend;
	}

}
