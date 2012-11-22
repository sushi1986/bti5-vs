package branch_access;

import mware_lib.NameServiceImpl;
import mware_lib.ObjectBroker;

public class ManagerRemote extends Manager {

	NameServiceImpl ns;
	String name;

	public ManagerRemote(String name, String serviceHost, int listenPort) {
		ObjectBroker ob = ObjectBroker.getBroker(serviceHost, listenPort);
		this.ns = (NameServiceImpl) ob.getNameService();
		this.name = name;
	}

	@Override
	public String createAccount(String owner) {
		String result = null;
		try {
			result = ns.callOnResolved(name, "createAccount", owner);
		} catch (Exception exc) {
		    if(exc instanceof RuntimeException) {
                throw (RuntimeException) exc;
            } else {
                return null;
            }
		}
		System.out.println(result);
		if (result != null) {
			return result;
		} else {
			return null;
		}
	}

	@Override
	public double getBalance(String accountID) {
		String result = null;
		try {
			result = ns.callOnResolved(name, "getBalance", accountID);
		} catch (Exception exc) {
		    if(exc instanceof RuntimeException) {
                throw (RuntimeException) exc;
            } else {
                return -1;
            }
		}
		if (result != null) {
			return new Double(result).doubleValue();
		} else {
			return -1;
		}
	}
}
