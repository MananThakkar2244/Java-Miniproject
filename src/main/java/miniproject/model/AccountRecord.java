package miniproject.model;

public class AccountRecord {

    private final Customer customer;
    private final LoanAccount account;

    public AccountRecord(Customer customer, LoanAccount account) {
        this.customer = customer;
        this.account = account;
    }

    public Customer getCustomer() {
        return customer;
    }

    public LoanAccount getAccount() {
        return account;
    }
}
