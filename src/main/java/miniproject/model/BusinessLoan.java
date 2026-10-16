package miniproject.model;

import java.time.LocalDate;

public class BusinessLoan extends LoanAccount {

    public BusinessLoan(String accountNumber, String customerId,
            LocalDate openDate, double loanAmount,
            double emiAmount, int dueDay, int emisMissed) {
        super(accountNumber, customerId, openDate, loanAmount, emiAmount, dueDay, emisMissed);
    }

    @Override
    public double getInterestRate() {
        return 9.0;
    }

    @Override
    public String getLoanTypeCode() {
        return "BL";
    }
}
