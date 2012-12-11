import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import threads.Message;
import threads.SendThread;

public class SenderOnlyTest {

    private static final String TEAM = "4";
    private static final String GROUP = "225.10.1.2";
    private static final int PORT = 15000;

    public static void main(String[] args) throws Exception {
        int port = PORT + Integer.valueOf(TEAM);

        BlockingQueue<Message> sndMsgs = new LinkedBlockingQueue<Message>();

        final SendThread s = new SendThread(sndMsgs, GROUP, port);

        s.start();

        TimerTask a = new TimerTask() {

            @Override
            public void run() {
                try {
                    s.getMsgsToSend().put(
                            new Message(new byte[] { 4 }, "42", (byte) 3, System
                                    .currentTimeMillis()));
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Timer b = new Timer(true);
        b.schedule(a, 500, 1000);

        try {
            s.join();
        }
        catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}
