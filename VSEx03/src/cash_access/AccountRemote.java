package cash_access;

import java.io.IOException;
import java.net.UnknownHostException;

import mware_lib.NameServiceImpl;
import mware_lib.ObjectBroker;

public class AccountRemote extends Account {

	NameServiceImpl ns;
	String name;

	public AccountRemote(String name, String serviceHost, int listenPort) {
		this.name = name;
		ObjectBroker ob = ObjectBroker.getBroker(serviceHost, listenPort);
		this.ns = (NameServiceImpl) ob.getNameService();
	}

	@Override
	public void deposit(double amount) {
		String result = null;
		try {
			result = ns.callOnResolved(name, "deposit", String.valueOf(amount));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result.equals("void")) {
			// alles OK
		} else {
			// nicht alles OK
		}
	}

	@Override
	public void withdraw(double amount) throws OverdraftException {
		String result = null;
		try {
			result = ns
					.callOnResolved(name, "withdraw", String.valueOf(amount));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result.equals("void")) {
			// alles OK
		} else {
			// nicht alles OK
		}
	}

	@Override
	public double getBalance() {
		String result = null;
		try {
			result = ns.callOnResolved(name, "getBalance");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result != null) {
			// alles OK
			return new Double(result).doubleValue();
		} else {
			// nicht alles OK
			return -1;
		}
	}

}
