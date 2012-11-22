package cash_access;

import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OverdraftException e) {
            e.printStackTrace();
        }
        if (result.equals("void")) {
            return;
        } else {
            System.out.println("[!!!] Error ... in deposit.");
        }
    }

    @Override
    public void withdraw(double amount) throws OverdraftException {
        String result = null;
        try {
            result = ns.callOnResolved(name, "withdraw", String.valueOf(amount));
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (OverdraftException e) {
            System.out.println("[!!!] Found it!!!!");
        }
        if (result.equals("void")) {
            return;
        } else {
            System.out.println("[!!!] Error ... in withdraw.");
        }
    }

    @Override
    public double getBalance() {
        String result = null;
        try {
            result = ns.callOnResolved(name, "getBalance");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } catch (OverdraftException e) {
            return -1;
        }
        if (result != null) {
            return new Double(result).doubleValue();
        } else {
            return -1;
        }
    }

}
