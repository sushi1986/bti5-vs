package threads;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class Test {

    /**
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public static void main(String[] args) throws InterruptedException, IOException {
    	
        System.out.println("Starting main for sending");
        MulticastSocket mSck = new MulticastSocket();
        byte[] buffer = new byte[10];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) (i + 'a');
        }
        DatagramPacket dp = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("225.10.1.2"), 15000);
        String tmp = new String(dp.getData());
        System.out.println("Now sending datagram with:\nto: " + dp.getAddress() + ":" + dp.getPort() + "\ncontains: "
                + tmp);
        mSck.send(dp);
        Thread.sleep(1000);

        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) ('j' - i);
        }
        dp = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("225.10.1.2"), 15000);
        mSck.send(dp);
        tmp = new String(dp.getData());
        System.out.println("Now sending datagram with:\nto: " + dp.getAddress() + ":" + dp.getPort() + "\ncontains: "
                + tmp);
        Thread.sleep(1000);
    }
}
