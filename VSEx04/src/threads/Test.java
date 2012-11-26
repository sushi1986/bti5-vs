package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Test {

	/**
	 * @param args
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static void main(String[] args) throws InterruptedException,
			IOException {
		ReceiveThread rt = new ReceiveThread("225.10.1.2", 15000);
		rt.start();

		MulticastSocket mSck = new MulticastSocket();
		byte[] buffer = new byte[10];

		Thread.sleep(1000);

		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) i;
		}

		DatagramPacket dp = new DatagramPacket(buffer, buffer.length,
				InetAddress.getByName("225.10.1.2"), 15000);
		mSck.send(dp);

		Thread.sleep(1000);

		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) (10 - i);
		}

		dp = new DatagramPacket(buffer, buffer.length,
				InetAddress.getByName("225.10.1.2"), 15000);
		mSck.send(dp);

		Thread.sleep(1000);
		
		rt.interrupt();
	}

}
