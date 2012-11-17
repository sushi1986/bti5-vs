package cash_access;

import mware_lib.NameServiceImpl;

public class AccountRemote extends Account {
    
    NameServiceImpl ns;
    
    public AccountRemote(NameServiceImpl ns) {
        this.ns = ns;
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
