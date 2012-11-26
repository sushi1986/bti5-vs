package threads;


public class MainReceive {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting main for receiving");
        ReceiveThread rt = new ReceiveThread("225.10.1.2", 15000);
        rt.start();
        rt.join();
    }
}
