package nameservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class NameServer {

	ServerSocket svrSocket;
	Map<String, Info> infos;

	NameServer() {
		infos = new HashMap<String, Info>();
		try {
			svrSocket = new ServerSocket(14001);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String putMsg(final String[] msg) {
		infos.put(msg[1], new Info(msg[1], msg[2], Integer.valueOf(msg[3])));
		return "ack::put";
	}

	private String getMsg(final String[] msg) {
		if (infos.containsKey(msg[1])) {
			Info tmp = infos.get(msg[1]);
			return "ack::get::" + tmp.getHost() + "::" + tmp.getPort();
		} else {
			return null;
		}
	}

	/*
	 * example messages: put::name::Address::Port -> ack::put get::name ->
	 * ack::get::address::port
	 */
	private String evaluateMessage(String msg) {
		String[] parts = msg.split("::");
		if (parts[0].equals("put") && parts.length == 4) {
			return putMsg(parts);
		} else if (parts[0].equals("get") && parts.length == 2) {
			return getMsg(parts);
		} else {
			return null;
		}
	}

	public boolean processNextMessage() {
		BufferedReader in;
		OutputStream out;
		Socket sck;
		String rc = null;
		try {
			sck = svrSocket.accept();
			in = new BufferedReader(new InputStreamReader(sck.getInputStream()));
			out = sck.getOutputStream();
			String message = in.readLine();
			System.out.println("[DBG] Received: '" + message + "'.");
			rc = evaluateMessage(message);
			if (rc != null) {
				System.out.println("[DBG] Answer: '" + rc + "'.");
				out.write(rc.getBytes());
			} else {
				System.out.println("[DBG] Failed to calculate answer to '"
						+ message + "'.");
			}
			in.close();
			out.close();
			sck.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (rc != null);
	}

	public static void main(String[] args) {
		NameServer ns = new NameServer();

		while (true) {
			ns.processNextMessage();
		}
	};

}