package miniproject.excelio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import miniproject.model.AccountRecord;
import miniproject.model.Customer;
import miniproject.model.LoanAccount;

// used for excel 
public class ExcelExporter {

    private static final String[] HEADERS = {
            "Account Number",
            "Open Date",
            "Loan Type",
            "Loan Amount",
            "Emi Amount",
            "Due Date",
            "Name",
            "Address",
            "Phone Number",
            "Email",
            "Unique ID",
            "EMIs Missed",
            "DPD",
            "DPD Bucket",
            "Paid Status"
    };

    // used for excel 
    public void export(List<AccountRecord> records, File file) throws IOException {
        List<AccountRecord> sorted = new ArrayList<>(records);
        sorted.sort(Comparator.comparing(r -> r.getAccount().getAccountNumber()));

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {

            Sheet sheet = workbook.createSheet("Account Details");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            DataFormat df = workbook.createDataFormat();

            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setDataFormat(df.getFormat("@"));

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(df.getFormat("dd-mm-yyyy"));

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int r = 0; r < sorted.size(); r++) {
                AccountRecord rec = sorted.get(r);
                Customer customer = rec.getCustomer();
                LoanAccount account = rec.getAccount();
                Row row = sheet.createRow(r + 1);

                Cell c0 = row.createCell(0);
                c0.setCellValue(account.getAccountNumber());
                c0.setCellStyle(textStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(account.getOpenDate());
                c1.setCellStyle(dateStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(account.getLoanTypeCode());

                Cell c3 = row.createCell(3);
                c3.setCellValue(account.getLoanAmount());

                Cell c4 = row.createCell(4);
                c4.setCellValue(account.getEmiAmount());

                Cell c5 = row.createCell(5);
                c5.setCellValue(account.getDueDay());

                Cell c6 = row.createCell(6);
                c6.setCellValue(customer.getName() != null ? customer.getName() : "");

                Cell c7 = row.createCell(7);
                c7.setCellValue(customer.getAddress() != null ? customer.getAddress() : "");

                Cell c8 = row.createCell(8);
                c8.setCellValue(customer.getPhone() != null ? customer.getPhone() : "");
                c8.setCellStyle(textStyle);

                Cell c9 = row.createCell(9);
                c9.setCellValue(customer.getEmail() != null ? customer.getEmail() : "");

                Cell c10 = row.createCell(10);
                c10.setCellValue(customer.getUniqueId());
                c10.setCellStyle(textStyle);

                Cell c11 = row.createCell(11);
                c11.setCellValue(account.getEmisMissed());

                Cell c12 = row.createCell(12);
                c12.setCellValue(account.getDpd());

                Cell c13 = row.createCell(13);
                c13.setCellValue(account.getBucket() != null ? account.getBucket() : "");

                Cell c14 = row.createCell(14);
                c14.setCellValue(account.getPaidStatus() != null ? account.getPaidStatus() : "");
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(0, sorted.size(), 0, HEADERS.length - 1));

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fos);
        }
    }
}
