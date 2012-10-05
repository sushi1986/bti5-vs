package client.controller;

import interfaces.IMessageServer;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import client.view.*;

/**
 * Controller for view. 
 * 
 * @author phillipgesien
 *
 */
public class Controller {

	private View view;
	private IMessageServer msgServer;
	private long id;

	public Controller(View view) {
		super();
		this.view = view;
		InetAddress ip;
		try {

			ip = InetAddress.getLocalHost();
			System.out.println("Current IP address : " + ip.getHostAddress());

			NetworkInterface network = NetworkInterface.getByInetAddress(ip);

			byte[] mac = network.getHardwareAddress();

			System.out.print("Current MAC address : ");

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < mac.length; i++) {
				sb.append(String.format("%02X%s", mac[i],
						(i < mac.length - 1) ? "-" : ""));
			}
			System.out.println(sb.toString());
			id = ip.getHostAddress().hashCode() + sb.toString().hashCode();

		} catch (UnknownHostException e) {

			e.printStackTrace();

		} catch (SocketException e) {

			e.printStackTrace();

		}
		System.out.println("Client ID: " + id);
	}

	/**
	 * connects to a server
	 * @param address the server adress
	 */
	public void connect(String address) {

		try {
			String name = "Message Server";
			Registry registry = LocateRegistry.getRegistry(address);
			msgServer = (IMessageServer) registry.lookup(name);
			System.out.println(" Client bound");
			view.getTxtrLeser().setText(
					view.getTxtrLeser().getText() + "\n"
							+ "Client connected to " + address);
		} catch (Exception e) {
			System.err.println("ComputePi exception:");
			e.printStackTrace();
		}

	}

	/**
	 * send a  message to the server
	 * @param msg message to send
	 */
	public void sendPressed(String msg) {
		System.out.println("send: clientID: " + id + " msg: " + msg);
		
		boolean error = true;
		//wegen Fehlersemantik:
		do{
		try {
			msgServer.addMessage("" + id, msg);
			error = false;
		} catch (RemoteException e) {
			System.err.println("could not send message");
		}
		}while(error);
	}

	public void updateTextview(String msg) {
		// TODO get msg from server and update view; change parameter?!
		view.getTxtrLeser().setText(view.getTxtrLeser().getText() + "\n" + msg);
	}

	/**
	 * asks a server for a new message
	 * @return false if there is no message
	 */
	public boolean receive() {
		try {
			String clientID = "" + id;
			String receive = msgServer.nextMessage(clientID);
			System.out.println("Received: " + receive);
			if (receive == null) {
				return false;
			} else {
				view.getTxtrLeser().setText(
						view.getTxtrLeser().getText() + "\n" + receive);
			}
		} catch (RemoteException e) {
			return false; // becaus of maybe
		}
		return true;

	}

	/**
	 * retrieves all available messages from server
	 */
	public void receiveAll() {
		// TODO Auto-generated method stub
		System.out.println("Receive all");
		while (receive());

	}

	@Override
	public String toString() {
		return "Controller [msgServer=" + msgServer + ", id=" + id + "]";
	}

}
