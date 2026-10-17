package miniproject;

import java.io.IOException;
import java.util.List;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import miniproject.dao.AccountDAO;
import miniproject.dao.AccountRepository;
import miniproject.excelio.ExcelImporter;
import miniproject.excelio.ImportResult;

class Menu {
    public static void main(String[] args) throws IOException {

        final String title = "Banking Collection System";
        String[] mainMenuOptions = {
                "Get customer details",
                "Add new customer",
                "Send Email",
                "Exit"
        };

        String[] customerDetailsOptions = {
                "Get details of a single customer",
                "Get all customer details",
                "Back"
        };

        String[] addCustomerOptions = {
                "Add details of single customer",
                "Import details from excel",
                "Back"
        };

        MenuScreen mainMenu = new MenuScreen(mainMenuOptions, title);
        MenuScreen custDetailsMenu = new MenuScreen(customerDetailsOptions, title);
        MenuScreen addCustMenu = new MenuScreen(addCustomerOptions, title);

        int terminalWidth = VariablesForMenu.TERMINALLENGTH; // this is needed DO NOT REMOVE THIS

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            terminal.enterRawMode();
            while (true) {
                int selectedIndex = mainMenu.getIndex(terminal);
                switch (selectedIndex) {
                    case 0:
                        // Get customer details
                        selectedIndex = custDetailsMenu.getIndex(terminal);
                        break;

                    case 1:
                        // Add new customer
                        selectedIndex = addCustMenu.getIndex(terminal);
                        if (selectedIndex == 1) {
                            handleExcelImport(terminal);
                        }
                        break;

                    case 2:
                        // Send email
                        System.out.println("Send Email");
                        break;

                    case 3:
                        // Exit
                        System.out.println("Program exited");
                        return;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.print(VariablesForMenu.SHOWCURSOR);
        }
    }

    private static void handleExcelImport(Terminal terminal) throws IOException {
        Attributes savedAttributes = terminal.getAttributes();

        terminal.setAttributes(terminal.enterRawMode());
        Attributes cooked = new Attributes(savedAttributes);
        terminal.setAttributes(cooked);

        System.out.print(VariablesForMenu.SHOWCURSOR);
        System.out.print(VariablesForMenu.MOVECURSOR);
        System.out.print(VariablesForMenu.ERASEBELOW);
        System.out.flush();

        org.jline.reader.impl.DefaultParser parser = new org.jline.reader.impl.DefaultParser();
        parser.setEscapeChars(new char[0]);

        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .parser(parser)
                .build();
        String path = reader.readLine("Enter path to .xlsx file: ").trim();
        System.out.println("Processing file, please wait...");
        System.out.flush();

        AccountRepository repo = new AccountDAO();
        ExcelImporter importer = new ExcelImporter();
        ImportResult result = importer.importFile(path, repo);

        System.out.println();
        System.out.println("=== Import Summary ===");
        System.out.println("Imported   : " + result.getImportedCount());
        System.out.println("Duplicates : " + result.getDuplicateCount());
        System.out.println("Failed     : " + result.getFailedCount());

        List<String> failures = result.getFailureMessages();
        if (!failures.isEmpty()) {
            System.out.println();
            System.out.println("Failure details (first 10):");
            int limit = Math.min(failures.size(), 10);
            for (int i = 0; i < limit; i++) {
                System.out.println("  " + failures.get(i));
            }
        }

        System.out.println();
        System.out.println("Press any key to continue...");
        System.out.flush();

        terminal.setAttributes(savedAttributes);
        terminal.enterRawMode();
        terminal.reader().read();
    }
}
