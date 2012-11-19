package branch_access;

import mware_lib.NameServiceImpl;
import mware_lib.ObjectBroker;

public class ManagerRemote extends Manager {

    NameServiceImpl ns;
	
	public ManagerRemote(String serviceHost, int listenPort) {
    	ObjectBroker ob = ObjectBroker.getBroker(serviceHost, listenPort);
        this.ns = (NameServiceImpl) ob.getNameService();
    }
	
	@Override
	public String createAccount(String owner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getBalance(String accountID) {
		// TODO Auto-generated method stub
		return 0;
	}

}
