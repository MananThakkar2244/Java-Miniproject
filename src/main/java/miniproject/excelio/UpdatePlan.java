package miniproject.excelio;

import java.util.ArrayList;
import java.util.List;

import miniproject.model.Customer;
import miniproject.model.LoanAccount;

public class UpdatePlan {

    public static class Entry {
        private final int rowNumber;
        private final Customer customer;
        private final LoanAccount account;

        public Entry(int rowNumber, Customer customer, LoanAccount account) {
            this.rowNumber = rowNumber;
            this.customer = customer;
            this.account = account;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public Customer getCustomer() {
            return customer;
        }

        public LoanAccount getAccount() {
            return account;
        }
    }

    private String fatalMessage;
    private final List<Entry> pendingUpdates = new ArrayList<>();
    private int unchangedCount;
    private final List<Entry> newAccounts = new ArrayList<>();
    private final List<String> failureMessages = new ArrayList<>();

    public void setFatalMessage(String fatalMessage) {
        this.fatalMessage = fatalMessage;
    }

    public String getFatalMessage() {
        return fatalMessage;
    }

    public void addPendingUpdate(int rowNumber, Customer customer, LoanAccount account) {
        pendingUpdates.add(new Entry(rowNumber, customer, account));
    }

    public List<Entry> getPendingUpdates() {
        return pendingUpdates;
    }

    public void addUnchanged() {
        unchangedCount++;
    }

    public int getUnchangedCount() {
        return unchangedCount;
    }

    public void addNewAccount(int rowNumber, Customer customer, LoanAccount account) {
        newAccounts.add(new Entry(rowNumber, customer, account));
    }

    public List<Entry> getNewAccounts() {
        return newAccounts;
    }

    public void addFailure(int rowNumber, String reason) {
        failureMessages.add("Row " + rowNumber + ": " + reason);
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }
}
