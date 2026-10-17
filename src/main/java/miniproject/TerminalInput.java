package miniproject;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;

class TerminalInput implements VariablesForMenu {

    static String readLine(Terminal terminal, String prompt, String prefill, boolean clearScreen) {
        Attributes savedAttributes = terminal.getAttributes();
        try {
            System.out.print(SHOWCURSOR);
            if (clearScreen) {
                System.out.print(MOVECURSOR);
                System.out.print(ERASEBELOW);
            }
            System.out.flush();

            DefaultParser parser = new DefaultParser();
            parser.setEscapeChars(new char[0]);

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .parser(parser)
                    .build();

            String line;
            if (prefill != null) {
                line = reader.readLine(prompt, null, prefill);
            } else {
                line = reader.readLine(prompt);
            }
            return line != null ? line.trim() : null;
        } catch (UserInterruptException | EndOfFileException e) {
            return null;
        } finally {
            terminal.setAttributes(savedAttributes);
            terminal.enterRawMode();
        }
    }
}
