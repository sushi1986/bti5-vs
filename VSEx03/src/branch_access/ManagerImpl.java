package branch_access;

import java.util.HashMap;
import java.util.Map;

import cash_access.Account;
import cash_access.AccountImpl;

public class ManagerImpl extends Manager {

    Map<String, Account> accounts;
    
    public ManagerImpl() {
        accounts = new HashMap<String,Account>();
    }
    
    @Override
    public String createAccount(String owner) {
        AccountImpl a =  new AccountImpl(owner);
        accounts.put(String.valueOf(a.getId()), a);
        return String.valueOf(a.getId());
    }

    @Override
    public double getBalance(String accountID) {
        return accounts.get(accountID).getBalance();
    }

}
