import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import threads.Message;
import threads.ReceiveThread;
import threads.SendThread;
import work.Worker;

public class Test {

	private static final String TEAM = "4";
	private static final String GROUP = "225.10.1.2";
	private static final int PORT = 15000;

	public static void main(String[] args) {
		int port = PORT + Integer.valueOf(TEAM);
		
		BlockingQueue<Message> rcvMsgs = new LinkedBlockingQueue<Message>();
		BlockingQueue<Message> sndMsgs = new LinkedBlockingQueue<Message>();
		
		final SendThread s = new SendThread(sndMsgs, GROUP, port);
		ReceiveThread r = new ReceiveThread(rcvMsgs, GROUP, port);
//		Thread t = new Worker(r.getReceivedMsgs(), s.getMsgsToSend(), TEAM);

		s.start();
		r.start();
//		t.start();
		
		TimerTask a = new TimerTask() {

			@Override
			public void run() {
				try {
					s.getMsgsToSend().put(new Message(new byte[]{1,2,3,4,5,6,7,8,9,0,1,2,3,4}, "me", (byte) 2, System.currentTimeMillis()));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		Timer b = new Timer(true); // true f�r DAEMON
//		b.schedule(a, 500, 1000);

		try {
			s.join();
			r.join();
//			t.join();
		} catch (Exception exc) {
			System.out.println("blub");
		}
	}
}
