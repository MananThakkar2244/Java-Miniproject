package miniproject;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.jline.terminal.Terminal;

import miniproject.model.AccountRecord;
import miniproject.model.Customer;
import miniproject.model.LoanAccount;

class AccountDetailScreen implements VariablesForMenu {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);
    private final AccountRecord record;
    private final NumberFormat nf = NumberFormat.getInstance(new Locale("en", "IN"));

    AccountDetailScreen(AccountRecord record) {
        this.record = record;
    }

    void show(Terminal terminal) throws IOException {
        while (true) {
            draw(terminal);
            int key = terminal.reader().read();
            if (key == 27) {
                int next = terminal.reader().read();
                if (next == 79 || next == 91) {
                    terminal.reader().read();
                }
            } else if (key == 13 || key == 10 || key == 'B' || key == 'b') {
                return;
            }
        }
    }

    private void draw(Terminal terminal) {
        int width = VariablesForMenu.getScreenWidth(terminal);

        Customer customer = record.getCustomer();
        LoanAccount account = record.getAccount();

        StringBuilder sb = new StringBuilder();
        String line = "-".repeat(width);

        sb.append(HIDECURSOR).append(MOVECURSOR);
        sb.append(ERASELINE).append(line).append(NEWLINE);

        String title = "Account Details";
        int titlePad = Math.max(0, (width - title.length()) / 2);
        sb.append(ERASELINE).append(" ".repeat(titlePad)).append(title).append(NEWLINE);
        sb.append(ERASELINE).append(line).append(NEWLINE);

        sb.append(ERASELINE).append(BOLD).append("Customer:").append(RESET).append(NEWLINE);
        appendField(sb, "Customer ID", customer.getUniqueId(), 14, width);
        appendField(sb, "Name", customer.getName(), 14, width);
        appendField(sb, "Address", sanitize(customer.getAddress()), 14, width);
        appendField(sb, "Phone", sanitize(customer.getPhone()), 14, width);
        appendField(sb, "Email", sanitize(customer.getEmail()), 14, width);

        sb.append(ERASELINE).append(NEWLINE);
        sb.append(ERASELINE).append(BOLD).append("Loan:").append(RESET).append(NEWLINE);
        appendField(sb, "Account Number", account.getAccountNumber(), 14, width);
        appendField(sb, "Loan Type", account.getLoanTypeCode(), 14, width);
        double rate = account.getInterestRate();
        String rateStr = (rate % 1 == 0 ? String.valueOf((int) rate) : String.valueOf(rate)) + "%";
        appendField(sb, "Interest Rate", rateStr, 14, width);
        appendField(sb, "Open Date", account.getOpenDate().format(DATE_FMT), 14, width);
        appendField(sb, "Loan Amount", "Rs. " + nf.format((long) account.getLoanAmount()), 14, width);
        appendField(sb, "EMI Amount", "Rs. " + nf.format((long) account.getEmiAmount()), 14, width);
        appendField(sb, "Due Day", getOrdinal(account.getDueDay()) + " of every month", 14, width);

        sb.append(ERASELINE).append(NEWLINE);
        sb.append(ERASELINE).append(BOLD).append("Overdue:").append(RESET).append(NEWLINE);
        appendField(sb, "EMIs Missed", String.valueOf(account.getEmisMissed()), 29, width);
        appendField(sb, "DPD", String.valueOf(account.getDpd()), 29, width);
        appendColoredField(sb, "Bucket", account.getBucket(), VariablesForMenu.getBucketColor(account.getBucket()), 29);
        appendColoredField(sb, "Status", account.getPaidStatus(), VariablesForMenu.getStatusColor(account.getPaidStatus()), 29);
        long overdueAmt = (long) (account.getEmiAmount() * account.getEmisMissed());
        appendField(sb, "Overdue Amount (EMI x missed)", "Rs. " + nf.format(overdueAmt), 29, width);

        sb.append(ERASELINE).append(NEWLINE);
        String backText = "> Back <";
        int backPad = Math.max(0, (width - backText.length()) / 2);
        sb.append(ERASELINE).append(" ".repeat(backPad)).append(REVERSE).append(backText).append(RESET).append(NEWLINE);
        sb.append(ERASEBELOW);

        System.out.print(sb);
        System.out.flush();
    }

    private void appendField(StringBuilder sb, String label, String value, int labelWidth, int screenWidth) {
        String text = String.format("  %-" + labelWidth + "s: %s", label, value);
        if (text.length() > screenWidth) {
            text = text.substring(0, screenWidth);
        }
        sb.append(ERASELINE).append(text).append(NEWLINE);
    }

    private void appendColoredField(StringBuilder sb, String label, String value, String color, int labelWidth) {
        sb.append(ERASELINE);
        sb.append(String.format("  %-" + labelWidth + "s: ", label));
        sb.append(color).append(value).append(RESET);
        sb.append(NEWLINE);
    }

    private String sanitize(String val) {
        if (val == null || val.trim().isEmpty()) {
            return "-";
        }
        return val.trim();
    }

    private String getOrdinal(int day) {
        if (day >= 11 && day <= 13) {
            return day + "th";
        }
        return switch (day % 10) {
            case 1 -> day + "st";
            case 2 -> day + "nd";
            case 3 -> day + "rd";
            default -> day + "th";
        };
    }
}
