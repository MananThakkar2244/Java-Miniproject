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

    final String newLine = System.lineSeparator();
    final int terminalLength = getTerminalLength();
    final String title = "Banking Collection System";
    final String[] options = {
            "Get customer details",
            "Add new customer",
            "Send Email",
            "Options",
            "Exit"
    };

    // terminal esc codes
    final String eraseLine = "\033[2K";
    final String hideCursor = "\033[?25l";
    final String moveCursor = "\033[H";
    final String eraseBelow = "\033[J";
}

class MethodsForMenu {
    String leftPadding(String option, int width) {
        return " ".repeat((width - option.length()) / 2) + option;
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

        String line = "-".repeat(terminalLength);
        StringBuilder frame = new StringBuilder();

        frame.append(hideCursor).append(moveCursor).append(eraseLine).append(line).append(newLine);
        frame.append(eraseLine).append(newLine);
        frame.append(eraseLine).append(leftPadding(title, terminalLength)).append(newLine);
        frame.append(eraseLine).append(newLine);
        frame.append(eraseLine).append(line).append(newLine).append(newLine);
        for (int i = 0; i < options.length; i++) {
            String text = (i == selectedIndex) ? "> " + options[i] + " <" : "  " + options[i] + "  ";
            frame.append(eraseLine).append(leftPadding(text, terminalLength)).append(newLine);
        }
        frame.append(eraseLine).append(newLine);
        frame.append(eraseLine).append(line).append(newLine).append(eraseBelow);
        System.out.print(frame);
        System.out.flush();
    }
}

class Menu {
    public static void main(String[] args) throws IOException {
        MainMenu mm = new MainMenu();
        int terminalWidth = VariablesForMenu.terminalLength; // this is needed DO NOT REMOVE THIS
        int index = 0;

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            terminal.enterRawMode();
            while (true) {
                mm.drawMainMenu(index);
                index = mm.startInput(index, terminal);
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
