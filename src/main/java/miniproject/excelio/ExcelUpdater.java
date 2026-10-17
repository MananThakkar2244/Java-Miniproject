package miniproject.excelio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import miniproject.dao.AccountRepository;
import miniproject.exception.AccountNotFoundException;
import miniproject.exception.DataAccessException;
import miniproject.exception.DuplicateAccountException;
import miniproject.model.AccountRecord;
import miniproject.model.Customer;
import miniproject.model.LoanAccount;

// used for excel 
public class ExcelUpdater {

    // used for excel 
    public UpdatePlan analyze(String path, AccountRepository repo) {
        UpdatePlan plan = new UpdatePlan();

        File file = new File(path);
        String fileErr = ExcelCells.validateFile(file, path);
        if (fileErr != null) {
            plan.setFatalMessage(fileErr);
            return plan;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                plan.setFatalMessage("Empty spreadsheet: no header row");
                return plan;
            }

            Map<String, Integer> columns = ExcelCells.mapHeaders(headerRow);
            String headerErr = ExcelCells.validateHeaders(columns);
            if (headerErr != null) {
                plan.setFatalMessage(headerErr);
                return plan;
            }

            Map<String, Integer> seenAccounts = new HashMap<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || ExcelCells.isRowEmpty(row)) {
                    continue;
                }
                int rowNum = i + 1;
                try {
                    processRow(row, rowNum, columns, seenAccounts, repo, plan);
                } catch (DataAccessException e) {
                    plan.setFatalMessage("Database is unreachable: " + e.getMessage());
                    return plan;
                } catch (IllegalArgumentException e) {
                    plan.addFailure(rowNum, e.getMessage());
                } catch (Exception e) {
                    plan.addFailure(rowNum, e.getMessage());
                }
            }

        } catch (DataAccessException e) {
            plan.setFatalMessage("Database is unreachable: " + e.getMessage());
        } catch (IOException e) {
            plan.setFatalMessage("Failed to read file: " + e.getMessage());
        }

        return plan;
    }

    private void processRow(Row row, int rowNum, Map<String, Integer> columns,
                            Map<String, Integer> seenAccounts, AccountRepository repo, UpdatePlan plan) {
        String accountNumber = ExcelCells.getTextValue(row, columns, "account number");
        String uniqueId = ExcelCells.getTextValue(row, columns, "unique id");
        String name = ExcelCells.getTextValue(row, columns, "name");
        String loanType = ExcelCells.getTextValue(row, columns, "loan type");

        if (accountNumber.isEmpty()) {
            plan.addFailure(rowNum, "Blank Account Number");
            return;
        }
        if (seenAccounts.containsKey(accountNumber)) {
            plan.addFailure(rowNum, "Duplicate account number in file (first seen in row " + seenAccounts.get(accountNumber) + ")");
            return;
        }
        seenAccounts.put(accountNumber, rowNum);

        if (uniqueId.isEmpty()) {
            plan.addFailure(rowNum, "Blank Unique ID");
            return;
        }
        if (name.isEmpty()) {
            plan.addFailure(rowNum, "Blank Name");
            return;
        }
        if (loanType.isEmpty()) {
            plan.addFailure(rowNum, "Blank Loan Type");
            return;
        }

        LocalDate openDate = ExcelCells.getDateValue(row, columns, "open date");
        if (openDate == null) {
            plan.addFailure(rowNum, "Invalid or blank Open Date");
            return;
        }

        int loanAmount = ExcelCells.getIntValue(row, columns, "loan amount");
        int emiAmount = ExcelCells.getIntValue(row, columns, "emi amount");
        int dueDay = ExcelCells.getIntValue(row, columns, "due date");

        if (loanAmount <= 0) {
            plan.addFailure(rowNum, "Invalid Loan Amount");
            return;
        }
        if (emiAmount <= 0) {
            plan.addFailure(rowNum, "Invalid EMI Amount");
            return;
        }
        if (dueDay < 1 || dueDay > 28) {
            plan.addFailure(rowNum, "Due Date must be 1-28, got: " + dueDay);
            return;
        }

        Integer emisMissedFromFile = null;
        if (columns.containsKey("emis missed")) {
            Integer colIndex = columns.get("emis missed");
            Cell cell = colIndex != null ? row.getCell(colIndex) : null;
            if (!ExcelCells.isCellBlank(cell)) {
                try {
                    emisMissedFromFile = ExcelCells.parseEmisMissed(cell);
                } catch (IllegalArgumentException e) {
                    plan.addFailure(rowNum, e.getMessage());
                    return;
                }
            }
        }

        try {
            AccountRecord existing = repo.findByAccountNumber(accountNumber);
            if (!uniqueId.equals(existing.getCustomer().getUniqueId())) {
                plan.addFailure(rowNum, "Customer ID in file (" + uniqueId + ") does not match the account's customer (" + existing.getCustomer().getUniqueId() + ")");
                return;
            }

            String fileAddress = ExcelCells.getTextValue(row, columns, "address");
            String address = fileAddress.isEmpty() ? existing.getCustomer().getAddress() : fileAddress;

            String filePhone = ExcelCells.getTextValue(row, columns, "phone number");
            String phone = filePhone.isEmpty() ? existing.getCustomer().getPhone() : filePhone;

            String fileEmail = ExcelCells.getTextValue(row, columns, "email");
            String email = fileEmail.isEmpty() ? existing.getCustomer().getEmail() : fileEmail;

            int emisMissed = emisMissedFromFile != null ? emisMissedFromFile : existing.getAccount().getEmisMissed();

            Customer newCustomer = new Customer(uniqueId, name, address, phone, email);
            LoanAccount newAccount = LoanAccount.fromCode(
                    loanType, accountNumber, uniqueId, openDate,
                    loanAmount, emiAmount, dueDay, emisMissed);
            newAccount.calculateDpd(LocalDate.now());

            boolean unchanged = Objects.equals(name, existing.getCustomer().getName())
                    && Objects.equals(address, existing.getCustomer().getAddress())
                    && Objects.equals(phone, existing.getCustomer().getPhone())
                    && Objects.equals(email, existing.getCustomer().getEmail())
                    && Objects.equals(loanType.toUpperCase(), existing.getAccount().getLoanTypeCode().toUpperCase())
                    && Objects.equals(openDate, existing.getAccount().getOpenDate())
                    && loanAmount == (int) existing.getAccount().getLoanAmount()
                    && emiAmount == (int) existing.getAccount().getEmiAmount()
                    && dueDay == existing.getAccount().getDueDay()
                    && emisMissed == existing.getAccount().getEmisMissed();

            if (unchanged) {
                plan.addUnchanged();
            } else {
                plan.addPendingUpdate(rowNum, newCustomer, newAccount);
            }

        } catch (AccountNotFoundException e) {
            String address = ExcelCells.getTextValue(row, columns, "address");
            String phone = ExcelCells.getTextValue(row, columns, "phone number");
            String email = ExcelCells.getTextValue(row, columns, "email");
            int emisMissed = emisMissedFromFile != null ? emisMissedFromFile : 1;

            Customer customer = new Customer(uniqueId, name, address, phone, email);
            LoanAccount account = LoanAccount.fromCode(
                    loanType, accountNumber, uniqueId, openDate,
                    loanAmount, emiAmount, dueDay, emisMissed);
            account.calculateDpd(LocalDate.now());

            plan.addNewAccount(rowNum, customer, account);
        }
    }

    public UpdateResult apply(UpdatePlan plan, boolean addNew, AccountRepository repo) {
        UpdateResult result = new UpdateResult();
        result.setUnchangedCount(plan.getUnchangedCount());

        for (String fail : plan.getFailureMessages()) {
            result.addFailure(fail);
        }

        for (UpdatePlan.Entry update : plan.getPendingUpdates()) {
            try {
                repo.updateRecord(update.getCustomer(), update.getAccount());
                result.addUpdated();
            } catch (AccountNotFoundException e) {
                result.addFailure(update.getRowNumber(), "Account not found: " + update.getAccount().getAccountNumber());
            } catch (DataAccessException e) {
                result.addFailure("Database error: " + e.getMessage());
                return result;
            }
        }

        if (addNew) {
            for (UpdatePlan.Entry newAcc : plan.getNewAccounts()) {
                try {
                    repo.insertAccount(newAcc.getCustomer(), newAcc.getAccount());
                    result.addAdded();
                } catch (DuplicateAccountException e) {
                    result.addFailure(newAcc.getRowNumber(), "Account already exists");
                } catch (DataAccessException e) {
                    result.addFailure("Database error: " + e.getMessage());
                    return result;
                }
            }
        } else {
            result.setSkippedNewCount(plan.getNewAccounts().size());
        }

        return result;
    }
}
