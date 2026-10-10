package miniproject;

import java.io.IOException;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class App {
    public static void main(String[] args) throws IOException {
        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            System.out.println("Terminal type: " + terminal.getType());
            System.out.println("Width (columns): " + terminal.getSize().getColumns());
            System.out.println("Height (rows): " + terminal.getSize().getRows());
        }
    }
}