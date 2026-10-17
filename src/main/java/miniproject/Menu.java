package miniproject;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import miniproject.dao.AccountDAO;
import miniproject.dao.AccountRepository;
import miniproject.excelio.ExcelExporter;
import miniproject.excelio.ExcelImporter;
import miniproject.excelio.ExcelUpdater;
import miniproject.excelio.ImportResult;
import miniproject.excelio.UpdatePlan;
import miniproject.excelio.UpdateResult;
import miniproject.exception.AccountNotFoundException;
import miniproject.exception.DataAccessException;
import miniproject.model.AccountRecord;

class Menu {

    interface SubmenuAction {
        void execute(int index) throws Exception;
    }

    public static void main(String[] args) throws IOException {

        final String title = "Banking Collection System";
        String[] mainMenuOptions = {
                "Get customer details",
                "Add new customer",
                "Update customer",
                "Exit"
        };

        String[] customerDetailsOptions = {
                "Get details of a single customer",
                "Get all customer details",
                "Export data to Excel",
                "Back"
        };

        String[] addCustomerOptions = {
                "Add details of single customer",
                "Import details from excel",
                "Back"
        };

        String[] updateCustomerOptions = {
                "Update a single customer",
                "Update multiple customers (Excel)",
                "Back"
        };

        MenuScreen mainMenu = new MenuScreen(mainMenuOptions, title);
        MenuScreen custDetailsMenu = new MenuScreen(customerDetailsOptions, title);
        MenuScreen addCustMenu = new MenuScreen(addCustomerOptions, title);
        MenuScreen updateCustMenu = new MenuScreen(updateCustomerOptions, title);

        int terminalWidth = VariablesForMenu.TERMINALLENGTH; // this is needed DO NOT REMOVE THIS

        AccountRepository repo = new AccountDAO();

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            terminal.enterRawMode();
            while (true) {
                int selectedIndex = mainMenu.getIndex(terminal);
                switch (selectedIndex) {
                    case 0:
                        runSubmenu(terminal, custDetailsMenu, idx -> {
                            if (idx == 0) {
                                String input = promptLine(terminal, "Enter account number or customer ID: ");
                                if (!input.isEmpty()) {
                                    try {
                                        AccountRecord rec = repo.findByAccountNumber(input);
                                        new AccountDetailScreen(rec).show(terminal);
                                    } catch (AccountNotFoundException e1) {
                                        try {
                                            List<AccountRecord> recs = repo.findByCustomerId(input);
                                            if (recs.size() == 1) {
                                                new AccountDetailScreen(recs.get(0)).show(terminal);
                                            } else {
                                                new CustomerListScreen("Accounts for customer " + input, recs)
                                                        .show(terminal);
                                            }
                                        } catch (AccountNotFoundException e2) {
                                            showMessage(terminal, "No account or customer found for " + input);
                                        }
                                    }
                                }
                            } else if (idx == 1) {
                                List<AccountRecord> records = repo.findAll();
                                if (records.isEmpty()) {
                                    showMessage(terminal, "No customers found. Import data first.");
                                } else {
                                    new CustomerListScreen("All Customers", records).show(terminal);
                                }
                            } else if (idx == 2) {
                                handleExport(terminal, repo);
                            }
                        });
                        break;

                    case 1:
                        runSubmenu(terminal, addCustMenu, idx -> {
                            if (idx == 0) {
                                CustomerEntryForm.run(terminal, repo);
                            } else if (idx == 1) {
                                handleExcelImport(terminal, repo);
                            }
                        });
                        break;

                    case 2:
                        runSubmenu(terminal, updateCustMenu, idx -> {
                            if (idx == 0) {
                                List<AccountRecord> records = repo.findAll();
                                if (records.isEmpty()) {
                                    showMessage(terminal, "No customers found. Import data first.");
                                } else {
                                    new CustomerListScreen("Update Customer - Select an Account", records, repo)
                                            .show(terminal);
                                }
                            } else if (idx == 1) {
                                handleExcelUpdate(terminal, repo);
                            }
                        });
                        break;

                    case 3:
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

    private static void runSubmenu(Terminal terminal, MenuScreen menu, SubmenuAction action) throws IOException {
        int backIndex = menu.options.length - 1;
        while (true) {
            try {
                int selectedIndex = menu.getIndex(terminal);
                if (selectedIndex == backIndex) {
                    break;
                }
                action.execute(selectedIndex);
            } catch (DataAccessException e) {
                showMessage(terminal, "Database error: " + e.getMessage());
            } catch (Exception e) {
                showMessage(terminal, "Error: " + e.getMessage());
            }
        }
    }

    private static void pressAnyKey(Terminal terminal) throws IOException {
        System.out.println("Press any key to continue...");
        System.out.flush();
        terminal.reader().read();
    }

    private static void printFailures(List<String> failures) {
        if (failures != null && !failures.isEmpty()) {
            System.out.println();
            System.out.println("Failure details (first 10):");
            int limit = Math.min(failures.size(), 10);
            for (int i = 0; i < limit; i++) {
                System.out.println("  " + failures.get(i));
            }
        }
    }

    private static void showMessage(Terminal terminal, String message) throws IOException {
        System.out.println(message);
        pressAnyKey(terminal);
    }

    private static String promptLine(Terminal terminal, String prompt) {
        String line = TerminalInput.readLine(terminal, prompt, null, true);
        return line != null ? line : "";
    }

    private static char promptKey(Terminal terminal, String prompt, String allowedChars) throws IOException {
        System.out.print(prompt);
        System.out.flush();
        String allowedLower = allowedChars.toLowerCase();
        while (true) {
            int key = terminal.reader().read();
            if (key == 27) {
                int next = terminal.reader().read();
                if (next == 79 || next == 91) {
                    terminal.reader().read();
                }
                continue;
            }
            char ch = Character.toLowerCase((char) key);
            if (allowedLower.indexOf(ch) >= 0) {
                System.out.println(ch);
                return ch;
            }
        }
    }

    // used for excel 
    private static void handleExport(Terminal terminal, AccountRepository repo) throws IOException {
        List<AccountRecord> records = repo.findAll();
        if (records.isEmpty()) {
            showMessage(terminal, "No data to export.");
            return;
        }

        String path = promptLine(terminal, "Enter output path: ");
        if (path == null) {
            return;
        }
        path = path.trim();
        if (path.startsWith("\"") && path.endsWith("\"") && path.length() >= 2) {
            path = path.substring(1, path.length() - 1).trim();
        }
        if (path.isEmpty()) {
            return;
        }

        File target = new File(path);
        if (target.isDirectory()) {
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            target = new File(target, "collecttrack_export_" + ts + ".xlsx");
        } else if (path.endsWith("/") || path.endsWith("\\")) {
            showMessage(terminal, "Error: Directory does not exist: " + path);
            return;
        } else {
            String name = target.getName();
            int dot = name.lastIndexOf('.');
            if (dot == -1) {
                target = new File(target.getParentFile(), name + ".xlsx");
            } else {
                String ext = name.substring(dot);
                if (!ext.equalsIgnoreCase(".xlsx")) {
                    showMessage(terminal, "Error: File must have a .xlsx extension.");
                    return;
                }
            }
        }

        File parent = target.getAbsoluteFile().getParentFile();
        if (parent != null && !parent.exists()) {
            showMessage(terminal, "Error: Parent folder does not exist: " + parent.getPath());
            return;
        }

        if (target.exists()) {
            char confirm = promptKey(terminal, "Overwrite? (Y/N): ", "yn");
            if (confirm != 'y') {
                return;
            }
        }

        try {
            ExcelExporter exporter = new ExcelExporter();
            exporter.export(records, target);
            System.out.println("Exported " + records.size() + " rows to " + target.getAbsolutePath());
            pressAnyKey(terminal);
        } catch (IOException e) {
            if (target.exists() && (e instanceof java.io.FileNotFoundException
                    || (e.getMessage() != null && (e.getMessage().toLowerCase().contains("used by another process")
                            || e.getMessage().toLowerCase().contains("access is denied"))))) {
                showMessage(terminal, "Close the file in Excel and try again.");
            } else {
                showMessage(terminal, "Error: " + e.getMessage());
            }
        }
    }

    // used for excel 
    private static void handleExcelImport(Terminal terminal, AccountRepository repo) throws IOException {
        String path = promptLine(terminal, "Enter path to .xlsx file: ");
        System.out.println("Processing file, please wait...");
        System.out.flush();

        ExcelImporter importer = new ExcelImporter();
        ImportResult result = importer.importFile(path, repo);

        System.out.println();
        System.out.println("=== Import Summary ===");
        System.out.println("Imported   : " + result.getImportedCount());
        System.out.println("Duplicates : " + result.getDuplicateCount());
        System.out.println("Failed     : " + result.getFailedCount());

        printFailures(result.getFailureMessages());

        System.out.println();
        pressAnyKey(terminal);
    }

    // used for excel 
    private static void handleExcelUpdate(Terminal terminal, AccountRepository repo) throws IOException {
        String path = promptLine(terminal, "Enter path to updated .xlsx file: ");
        if (path.isEmpty()) {
            return;
        }

        System.out.println("Reading file, please wait...");
        System.out.flush();

        ExcelUpdater updater = new ExcelUpdater();
        UpdatePlan plan = updater.analyze(path, repo);

        if (plan.getFatalMessage() != null) {
            showMessage(terminal, plan.getFatalMessage());
            return;
        }

        System.out.println();
        System.out.println("Existing accounts with changes : " + plan.getPendingUpdates().size());
        System.out.println("Existing accounts unchanged    : " + plan.getUnchangedCount());
        System.out.println("New accounts not in database   : " + plan.getNewAccounts().size());
        System.out.println("Invalid rows                   : " + plan.getFailureMessages().size());

        printFailures(plan.getFailureMessages());

        if (plan.getPendingUpdates().isEmpty() && plan.getNewAccounts().isEmpty()) {
            showMessage(terminal, "Nothing to update.");
            return;
        }

        boolean addNew = false;
        if (!plan.getNewAccounts().isEmpty()) {
            System.out.println();
            System.out.println("New accounts in file:");
            int listLimit = Math.min(plan.getNewAccounts().size(), 5);
            for (int i = 0; i < listLimit; i++) {
                System.out.println("  " + plan.getNewAccounts().get(i).getAccount().getAccountNumber());
            }
            if (plan.getNewAccounts().size() > 5) {
                System.out.println("  and " + (plan.getNewAccounts().size() - 5) + " more");
            }
            System.out.println();

            char choice;
            if (!plan.getPendingUpdates().isEmpty()) {
                choice = promptKey(terminal, "A = add them too, U = update existing accounts only, C = cancel: ",
                        "auc");
            } else {
                choice = promptKey(terminal, "A = add them too, C = cancel: ", "ac");
            }

            if (choice == 'c') {
                return;
            }
            addNew = (choice == 'a');
        }

        int updateCount = plan.getPendingUpdates().size();
        int addCount = addNew ? plan.getNewAccounts().size() : 0;
        System.out.println();
        System.out.println("About to update " + updateCount + " existing and add " + addCount + " new accounts");

        char confirm = promptKey(terminal, "Apply changes? (Y/N): ", "yn");
        if (confirm != 'y') {
            showMessage(terminal, "Update cancelled. Nothing was changed.");
            return;
        }

        UpdateResult result = updater.apply(plan, addNew, repo);
        System.out.println();
        System.out.println("=== Update Summary ===");
        System.out.println("Updated                 : " + result.getUpdatedCount());
        System.out.println("Unchanged               : " + result.getUnchangedCount());
        System.out.println("Added                   : " + result.getAddedCount());
        System.out.println("Skipped (new accounts)  : " + result.getSkippedNewCount());
        System.out.println("Failed                  : " + result.getFailedCount());

        printFailures(result.getFailureMessages());

        System.out.println();
        pressAnyKey(terminal);
    }
}
