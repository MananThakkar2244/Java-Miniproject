package miniproject;

import java.io.IOException;

import org.jline.terminal.Terminal;

class MenuScreen implements VariablesForMenu {

    String options[], title;

    MenuScreen(String options[], String title) {
        this.options = options;
        this.title = title;
    }

    String leftPadding(String option, int width) {
        return " ".repeat((width - option.length()) / 2) + option;
    }

    private int startInput(int selectedIndex, Terminal terminal) throws IOException {
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

    int getIndex(Terminal terminal) throws IOException {
        int index = 0;
        int result = 0;
        while (true) {
            drawMenu(index);
            result = startInput(index, terminal);
            if (result == -1) {
                return index;
            } else {
                index = result;
            }
        }
    }

    void drawMenu(int selectedIndex) {

        String line = "-".repeat(TERMINALLENGTH);
        StringBuilder frame = new StringBuilder();

        frame.append(HIDECURSOR).append(MOVECURSOR).append(ERASELINE).append(line).append(NEWLINE);
        frame.append(ERASELINE).append(NEWLINE);
        frame.append(ERASELINE).append(leftPadding(title, TERMINALLENGTH)).append(NEWLINE);
        frame.append(ERASELINE).append(NEWLINE);
        frame.append(ERASELINE).append(line).append(NEWLINE).append(NEWLINE);
        for (int i = 0; i < options.length; i++) {
            String text = (i == selectedIndex) ? "> " + options[i] + " <" : "  " + options[i] + "  ";
            frame.append(ERASELINE).append(leftPadding(text, TERMINALLENGTH)).append(NEWLINE);
        }
        frame.append(ERASELINE).append(NEWLINE);
        frame.append(ERASELINE).append(line).append(NEWLINE).append(ERASEBELOW);
        System.out.print(frame);
        System.out.flush();
    }
}