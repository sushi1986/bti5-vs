package cash_access;

import mware_lib.NameServiceImpl;
import mware_lib.ObjectBroker;

public class AccountRemote extends Account {
    
    NameServiceImpl ns;
    
    public AccountRemote(String serviceHost, int listenPort) {
    	ObjectBroker ob = ObjectBroker.getBroker(serviceHost, listenPort);
        this.ns = (NameServiceImpl) ob.getNameService();
    }
    
    
    
    @Override
    public void deposit(double amount) {
        
    }

    @Override
    public void withdraw(double amount) throws OverdraftException {
    }

    @Override
    public double getBalance() {
        return 0;
    }

}
