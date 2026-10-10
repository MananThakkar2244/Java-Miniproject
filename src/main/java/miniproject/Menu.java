package miniproject;

import java.io.IOException;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

interface VariablesForMenu {

    private static int getTerminalLength() {
        Terminal terminal;
        try {
            terminal = TerminalBuilder.builder().system(true).build();
        } catch (IOException e) {
            return 80; // fallback value for terminal length
        }
        return terminal.getSize().getColumns();
    }

    final int terminalLength = getTerminalLength();
    final String title = "Banking Collection System";
    final String[] options = {
            "Get customer details",
            "Add new customer",
            "Send Email",
            "Options",
            "Exit"
    };
}

class MethodsForMenu {

    void buildLine(int width) {
        for (int i = 0; i < width; i++) {
            System.out.print("-");
        }
    }

    String leftPadding(String option, int width) {
        return " ".repeat((width - option.length()) / 2) + option;
    }

    void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}

class MainMenu extends MethodsForMenu implements VariablesForMenu {

    void drawMainMenu() {
        clearConsole();
        buildLine(terminalLength);
        System.out.println();
        System.out.println();
        System.out.println(leftPadding(title, terminalLength));
        System.out.println();
        buildLine(terminalLength);
        System.out.println();
        System.out.println();
        for (int i = 0; i < options.length; i++) {
            System.out.println(leftPadding("> " + options[i] + " <", terminalLength));
        }
        System.out.println();
        buildLine(terminalLength);
    }
}

class Menu {
    public static void main(String[] args) throws IOException {
        MainMenu mm = new MainMenu();
        mm.drawMainMenu();
    }
}