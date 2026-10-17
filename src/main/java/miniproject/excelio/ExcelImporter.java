package miniproject.excelio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import miniproject.dao.AccountRepository;
import miniproject.exception.DataAccessException;
import miniproject.exception.DuplicateAccountException;
import miniproject.model.Customer;
import miniproject.model.LoanAccount;

public class ExcelImporter {

    public ImportResult importFile(String path, AccountRepository repo) {
        ImportResult result = new ImportResult();

        File file = new File(path);
        String fileErr = ExcelCells.validateFile(file, path);
        if (fileErr != null) {
            result.addFailure(0, fileErr);
            return result;
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.addFailure(0, "Empty spreadsheet: no header row");
                return result;
            }

            Map<String, Integer> columns = ExcelCells.mapHeaders(headerRow);
            String headerErr = ExcelCells.validateHeaders(columns);
            if (headerErr != null) {
                result.addFailure(0, headerErr);
                return result;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || ExcelCells.isRowEmpty(row)) {
                    continue;
                }
                processRow(row, i + 1, columns, repo, result);
            }

        } catch (DataAccessException e) {
            result.addFailure(0, "Database is unreachable: " + e.getMessage());
            return result;
        } catch (IOException e) {
            result.addFailure(0, "Failed to read file: " + e.getMessage());
        }

        return result;
    }

    private void processRow(Row row, int rowNum, Map<String, Integer> columns,
                            AccountRepository repo, ImportResult result) {
        try {
            String accountNumber = ExcelCells.getTextValue(row, columns, "account number");
            String uniqueId = ExcelCells.getTextValue(row, columns, "unique id");
            String name = ExcelCells.getTextValue(row, columns, "name");
            String loanType = ExcelCells.getTextValue(row, columns, "loan type");
            String phone = ExcelCells.getTextValue(row, columns, "phone number");

            if (accountNumber.isEmpty()) {
                result.addFailure(rowNum, "Blank Account Number");
                return;
            }
            if (uniqueId.isEmpty()) {
                result.addFailure(rowNum, "Blank Unique ID");
                return;
            }
            if (name.isEmpty()) {
                result.addFailure(rowNum, "Blank Name");
                return;
            }
            if (loanType.isEmpty()) {
                result.addFailure(rowNum, "Blank Loan Type");
                return;
            }

            LocalDate openDate = ExcelCells.getDateValue(row, columns, "open date");
            if (openDate == null) {
                result.addFailure(rowNum, "Invalid or blank Open Date");
                return;
            }

            int loanAmount = ExcelCells.getIntValue(row, columns, "loan amount");
            int emiAmount = ExcelCells.getIntValue(row, columns, "emi amount");
            int dueDay = ExcelCells.getIntValue(row, columns, "due date");

            if (loanAmount <= 0) {
                result.addFailure(rowNum, "Invalid Loan Amount");
                return;
            }
            if (emiAmount <= 0) {
                result.addFailure(rowNum, "Invalid EMI Amount");
                return;
            }
            if (dueDay < 1 || dueDay > 28) {
                result.addFailure(rowNum, "Due Date must be 1-28, got: " + dueDay);
                return;
            }

            int emisMissed = 1;
            if (columns.containsKey("emis missed")) {
                Integer colIndex = columns.get("emis missed");
                Cell cell = colIndex != null ? row.getCell(colIndex) : null;
                if (ExcelCells.isCellBlank(cell)) {
                    result.addFailure(rowNum, "EMIs Missed must be 1-3, got: blank");
                    return;
                }
                try {
                    emisMissed = ExcelCells.parseEmisMissed(cell);
                } catch (IllegalArgumentException e) {
                    result.addFailure(rowNum, e.getMessage());
                    return;
                }
            }

            String address = ExcelCells.getTextValue(row, columns, "address");
            String email = ExcelCells.getTextValue(row, columns, "email");

            Customer customer = new Customer(uniqueId, name, address, phone, email);
            LoanAccount account = LoanAccount.fromCode(
                    loanType, accountNumber, uniqueId, openDate,
                    loanAmount, emiAmount, dueDay, emisMissed);
            account.calculateDpd(LocalDate.now());

            repo.insertAccount(customer, account);
            result.addImported();

        } catch (DataAccessException e) {
            throw e;
        } catch (DuplicateAccountException e) {
            result.addDuplicate();
        } catch (IllegalArgumentException e) {
            result.addFailure(rowNum, e.getMessage());
        } catch (Exception e) {
            result.addFailure(rowNum, e.getMessage());
        }
    }
}
