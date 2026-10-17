package miniproject;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

import org.jline.terminal.Terminal;

import miniproject.dao.AccountRepository;
import miniproject.exception.AccountNotFoundException;
import miniproject.exception.DuplicateAccountException;
import miniproject.model.Customer;
import miniproject.model.LoanAccount;

class CustomerEntryForm implements VariablesForMenu {

    private static final NumberFormat NF = NumberFormat.getInstance(new Locale("en", "IN"));

    static void run(Terminal terminal, AccountRepository repo) throws IOException {
        System.out.print(SHOWCURSOR);
        System.out.print(MOVECURSOR);
        System.out.print(ERASEBELOW);
        System.out.flush();

        int width = VariablesForMenu.getScreenWidth(terminal);

        System.out.println("Add New Customer");
        System.out.println("-".repeat(width));
        System.out.println("Type cancel at any prompt to abort");
        System.out.println();

        Customer customer = null;
        while (customer == null) {
            String cidInput = TerminalInput.readLine(terminal, "Customer ID (max 10 chars): ", null, false);
            if (isCancel(cidInput)) {
                return;
            }
            String cid;
            try {
                cid = FieldValidator.customerId(cidInput);
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
                continue;
            }

            try {
                Customer existing = repo.findCustomer(cid);
                System.out.println("Customer found: " + existing.getName());
                while (true) {
                    String yn = TerminalInput.readLine(terminal, "Add a new loan to this existing customer? (Y/N): ", null, false);
                    if (isCancel(yn)) {
                        return;
                    }
                    if ("Y".equalsIgnoreCase(yn)) {
                        customer = existing;
                        break;
                    } else if ("N".equalsIgnoreCase(yn)) {
                        printError("Customer ID already in use. Enter a different ID.");
                        break;
                    }
                }
            } catch (AccountNotFoundException e) {
                String name = null;
                while (name == null) {
                    String in = TerminalInput.readLine(terminal, "Name (max 60 chars): ", null, false);
                    if (isCancel(in)) {
                        return;
                    }
                    try {
                        name = FieldValidator.name(in);
                    } catch (IllegalArgumentException ex) {
                        printError(ex.getMessage());
                    }
                }

                String address = null;
                while (address == null) {
                    String in = TerminalInput.readLine(terminal, "Address (max 100 chars): ", null, false);
                    if (isCancel(in)) {
                        return;
                    }
                    try {
                        address = FieldValidator.address(in);
                    } catch (IllegalArgumentException ex) {
                        printError(ex.getMessage());
                    }
                }

                String phone = null;
                while (phone == null) {
                    String in = TerminalInput.readLine(terminal, "Phone (10 to 15 digits): ", null, false);
                    if (isCancel(in)) {
                        return;
                    }
                    try {
                        phone = FieldValidator.phone(in);
                    } catch (IllegalArgumentException ex) {
                        printError(ex.getMessage());
                    }
                }

                String email = null;
                while (email == null) {
                    String in = TerminalInput.readLine(terminal, "Email (max 80 chars): ", null, false);
                    if (isCancel(in)) {
                        return;
                    }
                    try {
                        email = FieldValidator.email(in);
                    } catch (IllegalArgumentException ex) {
                        printError(ex.getMessage());
                    }
                }

                customer = new Customer(cid, name, address, phone, email);
            }
        }

        String accountNumber = null;
        while (accountNumber == null) {
            String in = TerminalInput.readLine(terminal, "Account Number (max 20 chars): ", null, false);
            if (isCancel(in)) {
                return;
            }
            String acc;
            try {
                acc = FieldValidator.accountNumber(in);
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
                continue;
            }

            try {
                repo.findByAccountNumber(acc);
                printError("Account already exists");
            } catch (AccountNotFoundException e) {
                accountNumber = acc;
            }
        }

        String loanType = null;
        while (loanType == null) {
            String in = TerminalInput.readLine(terminal, "Loan Type (CL, PL, BL, TWL): ", null, false);
            if (isCancel(in)) {
                return;
            }
            try {
                loanType = FieldValidator.loanType(in);
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
            }
        }

        LocalDate openDate = null;
        while (openDate == null) {
            String in = TerminalInput.readLine(terminal, "Open Date (dd-MM-yyyy): ", null, false);
            if (isCancel(in)) {
                return;
            }
            try {
                openDate = FieldValidator.openDate(in);
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
            }
        }

        int loanAmount = 0;
        while (loanAmount <= 0) {
            String in = TerminalInput.readLine(terminal, "Loan Amount: ", null, false);
            if (isCancel(in)) {
                return;
            }
            try {
                loanAmount = FieldValidator.loanAmount(in);
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
            }
        }

        int emiAmount = 0;
        while (emiAmount <= 0) {
            String in = TerminalInput.readLine(terminal, "EMI Amount: ", null, false);
            if (isCancel(in)) {
                return;
            }
            try {
                int emi = FieldValidator.emiAmount(in);
                FieldValidator.requireEmiNotAboveLoan(emi, loanAmount);
                emiAmount = emi;
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
            }
        }

        int dueDay = 0;
        while (dueDay < 1 || dueDay > 28) {
            String in = TerminalInput.readLine(terminal, "Due Day (1-28): ", null, false);
            if (isCancel(in)) {
                return;
            }
            try {
                dueDay = FieldValidator.dueDay(in);
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
            }
        }

        int emisMissed = -1;
        while (emisMissed < 0 || emisMissed > 3) {
            String in = TerminalInput.readLine(terminal, "EMIs Missed (0-3): ", null, false);
            if (isCancel(in)) {
                return;
            }
            try {
                emisMissed = FieldValidator.emisMissed(in);
            } catch (IllegalArgumentException e) {
                printError(e.getMessage());
            }
        }

        LoanAccount account = LoanAccount.fromCode(
                loanType, accountNumber, customer.getUniqueId(), openDate,
                loanAmount, emiAmount, dueDay, emisMissed);
        account.calculateDpd(LocalDate.now());

        System.out.println();
        System.out.println("=== Customer Details ===");
        System.out.println("Customer ID : " + customer.getUniqueId());
        System.out.println("Name        : " + customer.getName());
        System.out.println("Address     : " + customer.getAddress());
        System.out.println("Phone       : " + customer.getPhone());
        System.out.println("Email       : " + customer.getEmail());

        System.out.println();
        System.out.println("=== Loan Details ===");
        System.out.println("Account No  : " + account.getAccountNumber());
        System.out.println("Loan Type   : " + account.getLoanTypeCode());
        System.out.println("Open Date   : " + account.getOpenDate());
        System.out.println("Loan Amount : Rs. " + NF.format((long) account.getLoanAmount()));
        System.out.println("EMI Amount  : Rs. " + NF.format((long) account.getEmiAmount()));
        System.out.println("Due Day     : " + account.getDueDay());
        System.out.println("EMIs Missed : " + account.getEmisMissed());
        System.out.println("DPD         : " + account.getDpd());
        System.out.println("Bucket      : " + VariablesForMenu.getBucketColor(account.getBucket()) + account.getBucket() + RESET);
        System.out.println("Status      : " + VariablesForMenu.getStatusColor(account.getPaidStatus()) + account.getPaidStatus() + RESET);

        System.out.println();
        while (true) {
            String yn = TerminalInput.readLine(terminal, "Save this record? (Y/N): ", null, false);
            if (isCancel(yn) || "N".equalsIgnoreCase(yn)) {
                System.out.println("Not saved");
                System.out.println();
                System.out.println("Press any key to continue...");
                System.out.flush();
                terminal.reader().read();
                return;
            } else if ("Y".equalsIgnoreCase(yn)) {
                break;
            }
        }

        try {
            repo.insertAccount(customer, account);
            System.out.println();
            System.out.println(GREEN + "Saved successfully" + RESET + " - Account: " + account.getAccountNumber()
                    + ", DPD: " + account.getDpd() + ", Bucket: " + VariablesForMenu.getBucketColor(account.getBucket()) + account.getBucket() + RESET);
        } catch (DuplicateAccountException e) {
            printError("Account already exists");
        }

        System.out.println();
        System.out.println("Press any key to continue...");
        System.out.flush();
        terminal.reader().read();
    }

    private static boolean isCancel(String s) {
        return s == null || "cancel".equalsIgnoreCase(s);
    }

    private static void printError(String msg) {
        System.out.println(RED + msg + RESET);
    }
}
