package miniproject.excelio;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

// used for excel 
class ExcelCells {

    static final DataFormatter FORMATTER = new DataFormatter();

    static final String[] REQUIRED_HEADERS = {
            "account number", "open date", "loan type", "loan amount",
            "emi amount", "due date", "name", "unique id"
    };

    static String validateFile(File file, String path) {
        if (!file.exists()) {
            return "File does not exist: " + path;
        }
        if (!path.toLowerCase().endsWith(".xlsx")) {
            return "File must be a .xlsx Excel file: " + path;
        }
        return null;
    }

    static String validateHeaders(Map<String, Integer> columns) {
        for (String header : REQUIRED_HEADERS) {
            if (!columns.containsKey(header)) {
                return "Missing required column: " + header;
            }
        }
        return null;
    }

    static boolean isCellBlank(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return true;
        }
        return FORMATTER.formatCellValue(cell).trim().isEmpty();
    }

    static int parseEmisMissed(Cell cell) {
        String raw = FORMATTER.formatCellValue(cell).trim();
        if (cell.getCellType() == CellType.NUMERIC) {
            double d = cell.getNumericCellValue();
            if (d != Math.floor(d)) {
                throw new IllegalArgumentException("EMIs Missed must be 0-3, got: " + raw);
            }
            int val = (int) d;
            if (val < 0 || val > 3) {
                throw new IllegalArgumentException("EMIs Missed must be 0-3, got: " + raw);
            }
            return val;
        }
        try {
            int val = Integer.parseInt(raw);
            if (val < 0 || val > 3) {
                throw new IllegalArgumentException("EMIs Missed must be 0-3, got: " + raw);
            }
            return val;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("EMIs Missed must be 0-3, got: " + raw);
        }
    }

    static Map<String, Integer> mapHeaders(Row headerRow) {
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

    static boolean isRowEmpty(Row row) {
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

    static String getTextValue(Row row, Map<String, Integer> columns, String header) {
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

    static LocalDate getDateValue(Row row, Map<String, Integer> columns, String header) {
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

    static int getIntValue(Row row, Map<String, Integer> columns, String header) {
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
