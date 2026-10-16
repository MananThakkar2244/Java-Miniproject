package miniproject.model;

import java.time.LocalDate;

public class CarLoan extends LoanAccount {

    public CarLoan(String accountNumber, String customerId,
            LocalDate openDate, double loanAmount,
            double emiAmount, int dueDay, int emisMissed) {
        super(accountNumber, customerId, openDate, loanAmount, emiAmount, dueDay, emisMissed);
    }

    @Override
    public double getInterestRate() {
        return 10.0;
    }

    @Override
    public String getLoanTypeCode() {
        return "CL";
    }
}
