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

    static int getScreenWidth(Terminal terminal) {
        int width = terminal.getSize().getColumns();
        if (width <= 0) {
            width = TERMINALLENGTH;
        }
        return Math.max(width, 80);
    }

    static String getBucketColor(String bucket) {
        if (bucket == null) {
            return "";
        }
        return switch (bucket) {
            case "0-30" -> GREEN;
            case "31-60" -> YELLOW;
            case "61-90" -> RED;
            case "90+" -> MAGENTA;
            default -> "";
        };
    }

    static String getStatusColor(String status) {
        return "PAID".equalsIgnoreCase(status) ? GREEN : RED;
    }

    String NEWLINE = System.lineSeparator();
    int TERMINALLENGTH = getTerminalLength();

    // ANSI esc codes
    String ERASELINE = "\033[2K";
    String HIDECURSOR = "\033[?25l";
    String SHOWCURSOR = "\033[?25h";
    String MOVECURSOR = "\033[H";
    String ERASEBELOW = "\033[J";

    String RESET = "\033[0m";
    String BOLD = "\033[1m";
    String REVERSE = "\033[7m";
    String RED = "\033[31m";
    String GREEN = "\033[32m";
    String YELLOW = "\033[33m";
    String MAGENTA = "\033[35m";
}