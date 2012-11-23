package cash_access;

/**
 * VSP Lab03
 * @date 23.11.2012
 * @author Phillip Gesien, Raphael Hiesgen
 */

import mware_lib.Communicator;
import mware_lib.ObjectBroker;

public class AccountRemote extends Account {

    private final boolean DEBUG = false;

    Communicator ns;
    String name;

    public AccountRemote(String name, String serviceHost, int listenPort) {
        this.name = name;
        ObjectBroker ob = ObjectBroker.getBroker(serviceHost, listenPort);
        this.ns = (Communicator) ob.getNameService();
    }

    @Override
    public void deposit(double amount) {
        String result = null;
        result = ns.callOnResolved(name, "deposit", amount);
        if (result != null && result.startsWith("exc")) {
            String[] parts = result.split("::");
            String excName = parts[1];
            String excArgument = parts[2];
            if (DEBUG)
                System.out.println("[DBG] throwing exception: '" + excName + "' with argument '" + excArgument + "'.");
            Exception exc = null;
            try {
                exc = (Exception) Class.forName(excName).getConstructor(new Class<?>[] { String.class })
                        .newInstance(excArgument);
            } catch (Exception e) {
                if (DEBUG)
                    System.err.println("[!!!] Problem creating exception '" + excName + "'.");
                return;
            }
            if (exc instanceof RuntimeException) {
                throw (RuntimeException) exc;
            } else {
                return;
            }
        }
        return;
    }

    @Override
    public void withdraw(double amount) throws OverdraftException {
        String result = null;
        result = ns.callOnResolved(name, "withdraw", amount);
        if (result != null && result.startsWith("exc")) {
            String[] parts = result.split("::");
            String excName = parts[1];
            String excArgument = parts[2];
            if (DEBUG)
                System.out.println("[DBG] throwing exception: '" + excName + "' with argument '" + excArgument + "'.");
            Exception exc = null;
            try {
                exc = (Exception) Class.forName(excName).getConstructor(new Class<?>[] { String.class })
                        .newInstance(excArgument);
            } catch (Exception e) {
                if (DEBUG)
                    System.err.println("[!!!] Problem creating exception '" + excName + "'.");
                return;
            }
            if (exc instanceof RuntimeException) {
                throw (RuntimeException) exc;
            } else if (exc instanceof OverdraftException) {
                throw (OverdraftException) exc;
            } else {
                return;
            }
        }
        return;
    }

    @Override
    public double getBalance() {
        String result = null;
        result = ns.callOnResolved(name, "getBalance");
        if (result != null && result.startsWith("exc")) {
            String[] parts = result.split("::");
            String excName = parts[1];
            String excArgument = parts[2];
            if (DEBUG)
                System.out.println("[DBG] throwing exception: '" + excName + "' with argument '" + excArgument + "'.");
            Exception exc = null;
            try {
                exc = (Exception) Class.forName(excName).getConstructor(new Class<?>[] { String.class })
                        .newInstance(excArgument);
            } catch (Exception e) {
                if (DEBUG)
                    System.err.println("[!!!] Problem creating exception '" + excName + "'.");
                return -1;
            }
            if (exc instanceof RuntimeException) {
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
