package cash_access;

public class AccountImpl extends Account {
    private double amount;
    static long IDgen=0;
    private String owner;
    private long id;

    public AccountImpl(String owner) {
        amount = 0.0;
        this.owner = owner;
        id = IDgen++;
    }
    

    public AccountImpl(double amount, String owner) {
        this.amount = amount;
        this.owner = owner;
        id = IDgen++;
    }
    

    @Override
    public void deposit(double amount) {
        this.amount += amount;
    }

    @Override
    public void withdraw(double amount) throws OverdraftException {
        this.amount -= amount;
    }

    @Override
    public double getBalance() {
        return amount;
    }

    public long getId() {
        return id;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(amount);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AccountImpl other = (AccountImpl) obj;
        if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount))
            return false;
        if (id != other.id)
            return false;
        if (owner == null) {
            if (other.owner != null)
                return false;
        } else if (!owner.equals(other.owner))
            return false;
        return true;
    }


}
