package miniproject.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class LoanAccount implements Trackable {

    private final String accountNumber;
    private final String customerId;
    private final LocalDate openDate;
    private final double loanAmount;
    private final double emiAmount;
    private final int dueDay;
    private int emisMissed;
    private int dpd;
    private String bucket;
    private String paidStatus;

    protected LoanAccount(String accountNumber, String customerId,
            LocalDate openDate, double loanAmount,
            double emiAmount, int dueDay, int emisMissed) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.openDate = openDate;
        this.loanAmount = loanAmount;
        this.emiAmount = emiAmount;
        this.dueDay = dueDay;
        this.emisMissed = emisMissed;
        this.dpd = 0;
        this.bucket = "0-30";
        this.paidStatus = emisMissed == 0 ? "PAID" : "UNPAID";
    }

    public abstract double getInterestRate();

    public abstract String getLoanTypeCode();

    @Override
    public void calculateDpd(LocalDate today) {
        if (emisMissed <= 0) {
            this.dpd = 0;
            this.bucket = "0-30";
            this.paidStatus = "PAID";
            return;
        }

        LocalDate thisMonthDue = today.withDayOfMonth(dueDay);
        if (thisMonthDue.isAfter(today)) {
            thisMonthDue = thisMonthDue.minusMonths(1);
        }

        LocalDate oldestUnpaid = thisMonthDue.minusMonths(emisMissed - 1);
        this.dpd = (int) ChronoUnit.DAYS.between(oldestUnpaid, today);

        if (dpd <= 30) {
            this.bucket = "0-30";
        } else if (dpd <= 60) {
            this.bucket = "31-60";
        } else if (dpd <= 90) {
            this.bucket = "61-90";
        } else {
            this.bucket = "90+";
        }

        this.paidStatus = "UNPAID";
    }

    @Override
    public String getBucket() {
        return bucket;
    }

    @Override
    public String getPaidStatus() {
        return paidStatus;
    }

    public static LoanAccount fromCode(String code,
            String accountNumber, String customerId,
            LocalDate openDate, double loanAmount,
            double emiAmount, int dueDay, int emisMissed) {
        return switch (code.toUpperCase()) {
            case "CL" ->
                new CarLoan(accountNumber, customerId, openDate, loanAmount, emiAmount, dueDay, emisMissed);
            case "PL" ->
                new PersonalLoan(accountNumber, customerId, openDate, loanAmount, emiAmount, dueDay, emisMissed);
            case "BL" ->
                new BusinessLoan(accountNumber, customerId, openDate, loanAmount, emiAmount, dueDay, emisMissed);
            case "TWL" ->
                new TwoWheelerLoan(accountNumber, customerId, openDate, loanAmount, emiAmount, dueDay, emisMissed);
            default ->
                throw new IllegalArgumentException("Unknown loan type code: " + code);
        };
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public LocalDate getOpenDate() {
        return openDate;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public double getEmiAmount() {
        return emiAmount;
    }

    public int getDueDay() {
        return dueDay;
    }

    public int getEmisMissed() {
        return emisMissed;
    }

    public int getDpd() {
        return dpd;
    }

    @Override
    public String toString() {
        return getLoanTypeCode() + " [" + accountNumber + "] " +
                "loan=" + loanAmount + ", emi=" + emiAmount +
                ", missed=" + emisMissed + ", dpd=" + dpd +
                ", bucket=" + bucket + ", status=" + paidStatus;
    }
}
