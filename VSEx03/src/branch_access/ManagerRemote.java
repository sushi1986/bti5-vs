package branch_access;

import java.io.IOException;
import java.net.UnknownHostException;

import mware_lib.NameServiceImpl;
import mware_lib.ObjectBroker;

public class ManagerRemote extends Manager {

	NameServiceImpl ns;
	String name;

	public ManagerRemote(String name, String serviceHost, int listenPort) {
		this.name = name;

		ObjectBroker ob = ObjectBroker.getBroker(serviceHost, listenPort);
		this.ns = (NameServiceImpl) ob.getNameService();
	}

	@Override
	public String createAccount(String owner) {
		// TODO Auto-generated method stub
		String result = null;
		try {
			result = ns.callOnResolved(name, "createAccount", owner);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result != null) {
			// alles OK
			return result;
		} else {
			// nicht alles OK
			return null;
		}
	}

	@Override
	public double getBalance(String accountID) {
		String result = null;
		try {
			result = ns.callOnResolved(name, "getBalance", accountID);
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
