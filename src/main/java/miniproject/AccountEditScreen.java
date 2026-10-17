package miniproject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.jline.terminal.Terminal;

import miniproject.dao.AccountRepository;
import miniproject.exception.AccountNotFoundException;
import miniproject.exception.DataAccessException;
import miniproject.model.AccountRecord;
import miniproject.model.Customer;
import miniproject.model.LoanAccount;

class AccountEditScreen implements VariablesForMenu {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final String[] FIELD_LABELS = {
            "Name",
            "Address",
            "Phone",
            "Email",
            "Loan Type",
            "Open Date",
            "Loan Amount",
            "EMI Amount",
            "Due Day",
            "EMIs Missed"
    };

    private final AccountRecord originalRecord;
    private final AccountRepository repo;
    private final String customerId;
    private final String accountNumber;

    private final String[] originalValues = new String[10];
    private final String[] workingValues = new String[10];

    private int selectedIndex = 0;
    private String statusMessage = null;
    private boolean statusIsError = false;

    AccountEditScreen(AccountRecord record, AccountRepository repo) {
        this.originalRecord = record;
        this.repo = repo;

        Customer c = record.getCustomer();
        LoanAccount a = record.getAccount();

        this.customerId = c.getUniqueId();
        this.accountNumber = a.getAccountNumber();

        originalValues[0] = c.getName() != null ? c.getName() : "";
        originalValues[1] = c.getAddress() != null ? c.getAddress() : "";
        originalValues[2] = c.getPhone() != null ? c.getPhone() : "";
        originalValues[3] = c.getEmail() != null ? c.getEmail() : "";
        originalValues[4] = a.getLoanTypeCode();
        originalValues[5] = a.getOpenDate().format(DATE_FMT);
        originalValues[6] = String.valueOf((long) a.getLoanAmount());
        originalValues[7] = String.valueOf((long) a.getEmiAmount());
        originalValues[8] = String.valueOf(a.getDueDay());
        originalValues[9] = String.valueOf(a.getEmisMissed());

        System.arraycopy(originalValues, 0, workingValues, 0, 10);
    }

    String show(Terminal terminal) throws IOException {
        while (true) {
            draw(terminal);

            int readKey = terminal.reader().read();
            String currentStatus = statusMessage;
            statusMessage = null;

            if (readKey == 27) {
                readKey = terminal.reader().read();
                if (readKey == 79 || readKey == 91) {
                    readKey = terminal.reader().read();
                    if (readKey == 65) {
                        selectedIndex = (selectedIndex - 1 + FIELD_LABELS.length) % FIELD_LABELS.length;
                    } else if (readKey == 66) {
                        selectedIndex = (selectedIndex + 1) % FIELD_LABELS.length;
                    }
                }
            } else if (readKey == 13 || readKey == 10) {
                editSelectedField(terminal);
            } else if (readKey == 'P' || readKey == 'p') {
                workingValues[9] = "0";
                statusMessage = "EMIs missed set to 0 (PAID). Press S to save.";
                statusIsError = false;
            } else if (readKey == 'S' || readKey == 's') {
                String saved = trySave();
                if (saved != null) {
                    return saved;
                }
            } else if (readKey == 'B' || readKey == 'b') {
                if (!isChanged()) {
                    return null;
                }
                System.out.print(ERASELINE + "Discard changes? (Y/N): ");
                System.out.flush();
                int ch = terminal.reader().read();
                if (ch == 'Y' || ch == 'y') {
                    return null;
                }
            }
        }
    }

    private void editSelectedField(Terminal terminal) {
        String label = FIELD_LABELS[selectedIndex];
        String prefill = workingValues[selectedIndex];

        while (true) {
            String input = TerminalInput.readLine(terminal, label + ": ", prefill, false);
            if (input == null) {
                break;
            }
            try {
                String validated = validateAndNormalize(selectedIndex, input);
                workingValues[selectedIndex] = validated;
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(RED + e.getMessage() + RESET);
                prefill = input;
            }
        }
    }

    private String validateAndNormalize(int index, String input) {
        return switch (index) {
            case 0 -> FieldValidator.name(input);
            case 1 -> FieldValidator.address(input);
            case 2 -> FieldValidator.phone(input);
            case 3 -> FieldValidator.email(input);
            case 4 -> FieldValidator.loanType(input);
            case 5 -> FieldValidator.openDate(input).format(DATE_FMT);
            case 6 -> String.valueOf(FieldValidator.loanAmount(input));
            case 7 -> String.valueOf(FieldValidator.emiAmount(input));
            case 8 -> String.valueOf(FieldValidator.dueDay(input));
            case 9 -> String.valueOf(FieldValidator.emisMissed(input));
            default -> input;
        };
    }

    private String trySave() {
        if (!isChanged()) {
            statusMessage = "No changes to save";
            statusIsError = true;
            return null;
        }

        try {
            int loan = FieldValidator.loanAmount(workingValues[6]);
            int emi = FieldValidator.emiAmount(workingValues[7]);
            FieldValidator.requireEmiNotAboveLoan(emi, loan);

            String name = FieldValidator.name(workingValues[0]);
            String address = FieldValidator.address(workingValues[1]);
            String phone = FieldValidator.phone(workingValues[2]);
            String email = FieldValidator.email(workingValues[3]);
            String loanType = FieldValidator.loanType(workingValues[4]);
            LocalDate openDate = FieldValidator.openDate(workingValues[5]);
            int dueDay = FieldValidator.dueDay(workingValues[8]);
            int emisMissed = FieldValidator.emisMissed(workingValues[9]);

            Customer customer = new Customer(customerId, name, address, phone, email);
            LoanAccount account = LoanAccount.fromCode(loanType, accountNumber, customerId, openDate, loan, emi, dueDay, emisMissed);
            account.calculateDpd(LocalDate.now());

            repo.updateRecord(customer, account);
            return accountNumber;
        } catch (IllegalArgumentException | AccountNotFoundException | DataAccessException e) {
            statusMessage = e.getMessage();
            statusIsError = true;
            return null;
        }
    }

    private boolean isChanged() {
        for (int i = 0; i < 10; i++) {
            if (!Objects.equals(workingValues[i], originalValues[i])) {
                return true;
            }
        }
        return false;
    }

    private void draw(Terminal terminal) {
        int width = VariablesForMenu.getScreenWidth(terminal);

        StringBuilder sb = new StringBuilder();
        String line = "-".repeat(width);

        sb.append(HIDECURSOR).append(MOVECURSOR);
        sb.append(ERASELINE).append(line).append(NEWLINE);

        String title = "Update Account";
        int titlePad = Math.max(0, (width - title.length()) / 2);
        sb.append(ERASELINE).append(" ".repeat(titlePad)).append(title).append(NEWLINE);
        sb.append(ERASELINE).append(line).append(NEWLINE);

        sb.append(ERASELINE).append(String.format("Customer ID    : %s", customerId)).append(NEWLINE);
        sb.append(ERASELINE).append(String.format("Account Number : %s", accountNumber)).append(NEWLINE);
        sb.append(ERASELINE).append("Customer details (name, address, phone, email) apply to all accounts of this customer.").append(NEWLINE);
        sb.append(ERASELINE).append(NEWLINE);

        for (int i = 0; i < FIELD_LABELS.length; i++) {
            boolean isSelected = (i == selectedIndex);
            boolean changed = !Objects.equals(workingValues[i], originalValues[i]);
            String marker = isSelected ? "> " : "  ";
            String star = changed ? (YELLOW + "*" + RESET + (isSelected ? REVERSE : "")) : " ";

            StringBuilder row = new StringBuilder();
            if (isSelected) {
                row.append(REVERSE);
            }
            row.append(marker);
            row.append(String.format("%-14s: ", FIELD_LABELS[i]));
            row.append(workingValues[i]);
            row.append(" ").append(star);
            if (isSelected) {
                row.append(RESET);
            }

            String rowStr = row.toString();
            sb.append(ERASELINE).append(rowStr).append(NEWLINE);
        }

        sb.append(ERASELINE).append(NEWLINE);

        String dpdVal = "-";
        String bucketVal = "-";
        String statusVal = "-";
        try {
            int loan = FieldValidator.loanAmount(workingValues[6]);
            int emi = FieldValidator.emiAmount(workingValues[7]);
            String type = FieldValidator.loanType(workingValues[4]);
            LocalDate date = FieldValidator.openDate(workingValues[5]);
            int due = FieldValidator.dueDay(workingValues[8]);
            int missed = FieldValidator.emisMissed(workingValues[9]);

            LoanAccount preview = LoanAccount.fromCode(type, accountNumber, customerId, date, loan, emi, due, missed);
            preview.calculateDpd(LocalDate.now());

            dpdVal = String.valueOf(preview.getDpd());
            bucketVal = VariablesForMenu.getBucketColor(preview.getBucket()) + preview.getBucket() + RESET;
            statusVal = VariablesForMenu.getStatusColor(preview.getPaidStatus()) + preview.getPaidStatus() + RESET;
        } catch (Exception ignored) {
        }

        sb.append(ERASELINE).append(String.format("DPD     : %s (after save)", dpdVal)).append(NEWLINE);
        sb.append(ERASELINE).append(String.format("Bucket  : %s (after save)", bucketVal)).append(NEWLINE);
        sb.append(ERASELINE).append(String.format("Status  : %s (after save)", statusVal)).append(NEWLINE);

        sb.append(ERASELINE).append(line).append(NEWLINE);
        String help = "Up/Down: select   Enter: edit   P: mark paid   S: save   B: back";
        sb.append(ERASELINE).append(help).append(NEWLINE);

        if (statusMessage != null) {
            String color = statusIsError ? RED : GREEN;
            sb.append(ERASELINE).append(color).append(statusMessage).append(RESET).append(NEWLINE);
        } else {
            sb.append(ERASELINE).append(NEWLINE);
        }
        sb.append(ERASEBELOW);

        System.out.print(sb);
        System.out.flush();
    }
}
