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

    // terminal esc codes
    final String eraseLine = "\033[2K";
    final String hideCursor = "\033[?25l";
    final String moveCursor = "\033[H";
    final String eraseBelow = "\033[J";
}