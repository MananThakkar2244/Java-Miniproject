package miniproject.excelio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import miniproject.dao.AccountRepository;
import miniproject.exception.DuplicateAccountException;
import miniproject.model.Customer;
import miniproject.model.LoanAccount;

public class ExcelImporter {

    private static final DataFormatter FORMATTER = new DataFormatter();

    public ImportResult importFile(String path, AccountRepository repo) {
        ImportResult result = new ImportResult();

        File file = new File(path);
        if (!file.exists()) {
            result.addFailure(0, "File does not exist: " + path);
            return result;
        }
        if (!path.toLowerCase().endsWith(".xlsx")) {
            result.addFailure(0, "File must be a .xlsx Excel file: " + path);
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

            Map<String, Integer> columns = mapHeaders(headerRow);

            String[] required = {"account number", "open date", "loan type", "loan amount",
                    "emi amount", "due date", "name", "unique id"};
            for (String header : required) {
                if (!columns.containsKey(header)) {
                    result.addFailure(0, "Missing required column: " + header);
                    return result;
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                processRow(row, i + 1, columns, repo, result);
            }

        } catch (IOException e) {
            result.addFailure(0, "Failed to read file: " + e.getMessage());
        }

        return result;
    }

    private Map<String, Integer> mapHeaders(Row headerRow) {
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String header = FORMATTER.formatCellValue(cell).trim().toLowerCase();
                if (!header.isEmpty()) {
                    columns.put(header, i);
                }
            }
        }
        return columns;
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = FORMATTER.formatCellValue(cell).trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void processRow(Row row, int rowNum, Map<String, Integer> columns,
                            AccountRepository repo, ImportResult result) {
        try {
            String accountNumber = getTextValue(row, columns, "account number");
            String uniqueId = getTextValue(row, columns, "unique id");
            String name = getTextValue(row, columns, "name");
            String loanType = getTextValue(row, columns, "loan type");
            String phone = getTextValue(row, columns, "phone number");

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

            LocalDate openDate = getDateValue(row, columns, "open date");
            if (openDate == null) {
                result.addFailure(rowNum, "Invalid or blank Open Date");
                return;
            }

            int loanAmount = getIntValue(row, columns, "loan amount");
            int emiAmount = getIntValue(row, columns, "emi amount");
            int dueDay = getIntValue(row, columns, "due date");

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
                int val = getIntValue(row, columns, "emis missed");
                if (val > 0) {
                    emisMissed = val;
                }
            }

            String address = getTextValue(row, columns, "address");
            String email = getTextValue(row, columns, "email");

            Customer customer = new Customer(uniqueId, name, address, phone, email);
            LoanAccount account = LoanAccount.fromCode(
                    loanType, accountNumber, uniqueId, openDate,
                    loanAmount, emiAmount, dueDay, emisMissed);
            account.calculateDpd(LocalDate.now());

            repo.insertAccount(customer, account);
            result.addImported();

        } catch (DuplicateAccountException e) {
            result.addDuplicate();
        } catch (IllegalArgumentException e) {
            result.addFailure(rowNum, e.getMessage());
        } catch (Exception e) {
            result.addFailure(rowNum, e.getMessage());
        }
    }

    private String getTextValue(Row row, Map<String, Integer> columns, String header) {
        Integer colIndex = columns.get(header);
        if (colIndex == null) {
            return "";
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return "";
        }
        return FORMATTER.formatCellValue(cell).trim();
    }

    private LocalDate getDateValue(Row row, Map<String, Integer> columns, String header) {
        Integer colIndex = columns.get(header);
        if (colIndex == null) {
            return null;
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }

    private int getIntValue(Row row, Map<String, Integer> columns, String header) {
        Integer colIndex = columns.get(header);
        if (colIndex == null) {
            return 0;
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return 0;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        try {
            return Integer.parseInt(FORMATTER.formatCellValue(cell).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
