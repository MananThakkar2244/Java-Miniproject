package miniproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

interface VariablesForMenu {

    private static int getTerminalLength() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            Process p;
            if (os.contains("win")) {
                p = new ProcessBuilder("powershell", "-command",
                        "$Host.UI.RawUI.WindowSize.Width").start();
            } else {
                p = new ProcessBuilder("sh", "-c", "stty size < /dev/tty").start();
            }
            try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = r.readLine();
                if (line == null) {
                    return 80;
                }
                if (os.contains("win")) {
                    return Integer.parseInt(line.trim());
                }
                return Integer.parseInt(line.trim().split(" ")[1]);
            }
        } catch (Exception e) {
            return 80; // fallback
        }
    }

    // Variables
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

        System.out.println();
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            System.out.println("JLine width:      " + terminal.getSize().getColumns());
            System.out.println("Old method width: " + VariablesForMenu.terminalLength);
        }
    }
}