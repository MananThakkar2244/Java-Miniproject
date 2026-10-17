package miniproject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

class FieldValidator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    static String customerId(String input) {
        if (input == null || input.isEmpty() || input.length() > 10 || input.contains(" ")) {
            throw new IllegalArgumentException("Customer ID must be 1 to 10 characters with no spaces.");
        }
        return input;
    }

    static String name(String input) {
        if (input == null || input.isEmpty() || input.length() > 60) {
            throw new IllegalArgumentException("Name is required (max 60 chars).");
        }
        return input;
    }

    static String address(String input) {
        if (input == null || input.isEmpty() || input.length() > 100) {
            throw new IllegalArgumentException("Address is required (max 100 chars).");
        }
        return input;
    }

    static String phone(String input) {
        if (input == null || !input.matches("\\d{10,15}")) {
            throw new IllegalArgumentException("Phone must be digits only, 10 to 15 digits.");
        }
        return input;
    }

    static String email(String input) {
        if (input == null || input.isEmpty() || input.length() > 80 || !input.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Email is required, up to 80 characters, with one '@' and a dot after it.");
        }
        return input;
    }

    static String accountNumber(String input) {
        if (input == null || input.isEmpty() || input.length() > 20 || input.contains(" ")) {
            throw new IllegalArgumentException("Account Number must be 1 to 20 characters with no spaces.");
        }
        return input;
    }

    static String loanType(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Loan Type must be one of CL, PL, BL, TWL.");
        }
        String upper = input.toUpperCase();
        if (!"CL".equals(upper) && !"PL".equals(upper) && !"BL".equals(upper) && !"TWL".equals(upper)) {
            throw new IllegalArgumentException("Loan Type must be one of CL, PL, BL, TWL.");
        }
        return upper;
    }

    static LocalDate openDate(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Open Date must be in dd-MM-yyyy format and not in the future.");
        }
        try {
            LocalDate date = LocalDate.parse(input, DATE_FMT);
            if (date.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("Open Date must not be in the future.");
            }
            return date;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Open Date must be in valid dd-MM-yyyy format.");
        }
    }

    static int loanAmount(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Loan Amount must be a whole number greater than 0.");
        }
        try {
            int val = Integer.parseInt(input);
            if (val <= 0) {
                throw new IllegalArgumentException("Loan Amount must be a whole number greater than 0.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Loan Amount must be a valid whole number.");
        }
    }

    static int emiAmount(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("EMI Amount must be a whole number greater than 0.");
        }
        try {
            int val = Integer.parseInt(input);
            if (val <= 0) {
                throw new IllegalArgumentException("EMI Amount must be a whole number greater than 0.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("EMI Amount must be a valid whole number.");
        }
    }

    static int dueDay(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Due Day must be a whole number from 1 to 28.");
        }
        try {
            int val = Integer.parseInt(input);
            if (val < 1 || val > 28) {
                throw new IllegalArgumentException("Due Day must be a whole number from 1 to 28.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Due Day must be a valid whole number from 1 to 28.");
        }
    }

    static int emisMissed(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("EMIs Missed must be a whole number from 0 to 3.");
        }
        try {
            int val = Integer.parseInt(input);
            if (val < 0 || val > 3) {
                throw new IllegalArgumentException("EMIs Missed must be a whole number from 0 to 3.");
            }
            return val;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("EMIs Missed must be a valid whole number from 0 to 3.");
        }
    }

    static void requireEmiNotAboveLoan(int emi, int loan) {
        if (emi > loan) {
            throw new IllegalArgumentException("EMI Amount cannot be more than Loan Amount.");
        }
    }
}
