package miniproject;

import java.io.*;

import org.jline.terminal.*;;

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
        DBoperations db = new DBoperations();

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
                        break;

                    case 2:
                        // Send email
                        System.out.println("Send Email");
                        break;

                    case 3:
                        // Exit
                        db.connectDB();
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
}
