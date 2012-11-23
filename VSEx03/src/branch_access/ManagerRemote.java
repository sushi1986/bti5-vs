package branch_access;

import mware_lib.Communicator;
import mware_lib.ObjectBroker;

public class ManagerRemote extends Manager {

    private final boolean DEBUG = false;
    
    Communicator ns;
    String name;

    public ManagerRemote(String name, String serviceHost, int listenPort) {
        ObjectBroker ob = ObjectBroker.getBroker(serviceHost, listenPort);
        this.ns = (Communicator) ob.getNameService();
        this.name = name;
    }

    @Override
    public String createAccount(String owner) {
        String result = null;
        result = ns.callOnResolved(name, "createAccount", owner);
        if (result != null && result.startsWith("exc")) {
            String[] parts = result.split("::");
            String excName = parts[1];
            String excArgument = parts[2];
            if (DEBUG) System.out.println("[DBG] throwing exception: '" + excName + "' with argument '" + excArgument + "'.");
            Exception exc = null;
            try {
                exc = (Exception) Class.forName(excName).getConstructor(new Class<?>[] { String.class }).newInstance(excArgument);
            } catch (Exception e) {
                if (DEBUG) System.err.println("[!!!] Problem creating exception '" + excName +"'.");
                return null;
            }
            if(exc instanceof RuntimeException) {
                throw (RuntimeException) exc;
            } else {
                return null;
            }
        }
        if (DEBUG) System.out.println("[DBG] CreatAccount result: '"+ result +"'.");
        if (result != null) {
            return result;
        } else {
            return null;
        }
    }

    @Override
    public double getBalance(String accountID) {
        String result = null;
        result = ns.callOnResolved(name, "getBalance", accountID);
        if (result != null && result.startsWith("exc")) {
            String[] parts = result.split("::");
            String excName = parts[1];
            String excArgument = parts[2];
            if (DEBUG) System.out.println("[DBG] throwing exception: '" + excName + "' with argument '" + excArgument + "'.");
            Exception exc = null;
            try {
                exc = (Exception) Class.forName(excName).getConstructor(new Class<?>[] { String.class }).newInstance(excArgument);
            } catch (Exception e) {
                if (DEBUG) System.err.println("[!!!] Problem creating exception '" + excName +"'.");
                return -1;
            }
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
