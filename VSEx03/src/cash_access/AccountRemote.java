package cash_access;

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
        result = ns.callOnResolved(name, "deposit", amount);
        if (result != null && result.startsWith("exc")) {
            String[] parts = result.split("::");
            String excName = parts[1];
            String excArgument = parts[2];
            System.out.println("[!!!] throwing exception: '" + excName + "' with argument '" + excArgument + "'.");
            Exception exc = null;
            try {
                exc = (Exception) Class.forName(excName).getConstructor(new Class<?>[] { String.class }).newInstance(excArgument);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            if(exc instanceof RuntimeException) {
                throw (RuntimeException) exc;
            } else {
                return;
            }
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
        result = ns.callOnResolved(name, "withdraw", amount);
        if (result != null && result.startsWith("exc")) {
            String[] parts = result.split("::");
            String excName = parts[1];
            String excArgument = parts[2];
            System.out.println("[!!!] throwing exception: '" + excName + "' with argument '" + excArgument + "'.");
            Exception exc = null;
            try {
                exc = (Exception) Class.forName(excName).getConstructor(new Class<?>[] { String.class }).newInstance(excArgument);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            if(exc instanceof RuntimeException) {
                throw (RuntimeException) exc;
            } else if (exc instanceof OverdraftException) {
                throw (OverdraftException) exc;
            }else {
                return;
            }
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
