package miniproject;

import java.io.IOException;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

class Menu {
    public static void main(String[] args) throws IOException {

        final String title = "Banking Collection System";
        String[] mainMenuOptions = {
                "Get customer details",
                "Add new customer",
                "Send Email",
                "Options",
                "Exit"
        };

        MenuScreen mainMenu = new MenuScreen(mainMenuOptions, title);
        int terminalWidth = VariablesForMenu.terminalLength; // this is needed DO NOT REMOVE THIS
        int index = 0;

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            terminal.enterRawMode();
            while (true) {
                mainMenu.drawMenu(index);
                index = mainMenu.startInput(index, terminal);
                if (index == -1) {
                    System.out.println("Enter has been pressed");
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.print("\033[?25h"); // show cursor
        }
    }
}
