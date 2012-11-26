package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ReceiveThread extends Thread {

	MulticastSocket mSck;

	public ReceiveThread(String group, int port) {
		try {
			mSck = new MulticastSocket(port);
			mSck.joinGroup(InetAddress.getByName(group));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		byte[] buffer = new byte[1024];
		while (!isInterrupted()) {

			// receive
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			try {
				mSck.receive(dp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.write(dp.getData(),0,dp.getData().length);
			System.out.write(buffer,0,buffer.length);

			// validiere

		}

	}
}
