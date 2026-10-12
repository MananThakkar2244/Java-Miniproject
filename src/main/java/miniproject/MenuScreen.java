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

    void drawMenu(int selectedIndex) {

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