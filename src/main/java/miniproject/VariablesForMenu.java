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

    String NEWLINE = System.lineSeparator();
    int TERMINALLENGTH = getTerminalLength();

    // ANSI esc codes
    String ERASELINE = "\033[2K";
    String HIDECURSOR = "\033[?25l";
    String SHOWCURSOR = "\033[?25h";
    String MOVECURSOR = "\033[H";
    String ERASEBELOW = "\033[J";
}