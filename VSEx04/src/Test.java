import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import threads.Message;
import threads.ReceiveThread;
import threads.SendThread;
import work.Worker;

public class Test {

	private static final String GROUP = "225.10.1.2";
	private static final int PORT = 15000;

	private static String readTeam() {
		BufferedInputStream bis = new BufferedInputStream(System.in);
		byte[] input = new byte[24];
		String resu = null;
		try {
			if (bis.read(input) != 24) {
				System.out.println("Fehler mit der DatenQuelle");
			} else {
				resu = new String(Arrays.copyOfRange(input, 0, 10));

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resu;
	}

	public static void main(String[] args) {
		int port = PORT;

		String team = readTeam();
		if (team != null) {
			port += Integer.valueOf(team.substring(5, 7));
		}

		BlockingQueue<Message> rcvMsgs = new LinkedBlockingQueue<Message>();
		BlockingQueue<Message> sndMsgs = new LinkedBlockingQueue<Message>();

		final SendThread s = new SendThread(sndMsgs, GROUP, port);
		ReceiveThread r = new ReceiveThread(rcvMsgs, GROUP, port);
		Thread t = new Worker(r.getReceivedMsgs(), s.getMsgsToSend(), team);

		s.start();
		r.start();
		t.start();

		try {
			s.join();
			r.join();
			t.join();
		} catch (Exception exc) {
			System.out.println("blub");
		}
	}
}
