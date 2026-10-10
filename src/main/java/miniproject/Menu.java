package miniproject;

import java.io.IOException;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

interface VariablesForMenu {

    private static int getTerminalLength() {
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            return terminal.getSize().getColumns();
        } catch (IOException e) {
            return 80; // fallback value for terminal length
        }
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
        try {
            new ProcessBuilder("cmd", "/c", "cls")
                    .inheritIO()
                    .start()
                    .waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class MainMenu extends MethodsForMenu implements VariablesForMenu {

    int startInput(int selectedIndex, Terminal terminal) throws IOException {
        int readKey = terminal.reader().read();
        if (readKey == 27) {
            readKey = terminal.reader().read();
            if (readKey == 79 || readKey == 91) {
                readKey = terminal.reader().read();
                if (readKey == 65) {
                    selectedIndex = (selectedIndex - 1 + options.length) % options.length; // go up
                } else if (readKey == 66) {
                    selectedIndex = (selectedIndex + 1) % options.length; // go down
                }
            }
        } else if (readKey == 13 || readKey == 10) {
            return -1; // enter has been pressed
        }
        return selectedIndex;
    }

    void drawMainMenu(int selectedIndex) {
        clearConsole();
        System.out.print("\033[?25l"); // hide cursor
        buildLine(terminalLength);
        System.out.println();
        System.out.println();
        System.out.println(leftPadding(title, terminalLength));
        System.out.println();
        buildLine(terminalLength);
        System.out.println();
        System.out.println();
        for (int i = 0; i < options.length; i++) {
            if (selectedIndex == i) {
                System.out.println(leftPadding("> " + options[i] + " <", terminalLength));
            } else {
                System.out.println(leftPadding("  " + options[i] + "  ", terminalLength));
            }
        }
        System.out.println();
        buildLine(terminalLength);
    }
}

class Menu {
    public static void main(String[] args) throws IOException {
        MainMenu mm = new MainMenu();
        int terminalWidth = VariablesForMenu.terminalLength;
        int index = 0;

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            terminal.enterRawMode();
            while (true) {
                mm.drawMainMenu(index);
                int indexVar = mm.startInput(index, terminal);
                if (indexVar == -1) {
                    System.out.println("Enter has been pressed");
                    System.out.print("\033[?25h"); // show cursor
                    break;
                } else {
                    index = indexVar;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
