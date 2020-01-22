package com.project.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static com.project.scanner.ScannerDFA.BLOCK_COMMENT;
import static com.project.scanner.ScannerDFA.BLOCK_STAR;
import static com.project.scanner.ScannerDFA.ERRSTATE;
import static com.project.scanner.ScannerDFA.START_STATE;

public class JavaScanner {

    public static ArrayList<Token> tokenizeFile(final String fileName) {
        Scanner fileScanner = null;

        // Load the file into a string scanner.
        try {
            File code = new File(fileName);
            fileScanner = new Scanner(code);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(42);
        }

        final ArrayList<Token> tokenList = new ArrayList<>();

        boolean inBlockComment = false;
        while (fileScanner.hasNextLine()) {

            String line = fileScanner.nextLine();
            while (!line.isEmpty()) {

                // Used to checkpoint last accepted state in DFA.
                int rollbackMarker = 0;
                State rollbackState = null;

                // If we are in a block quote, we want to stay in until we exit.
                State state = inBlockComment ? BLOCK_COMMENT : START_STATE;

                for (int i = 0; i < line.length(); ++i) {

                    // Fetch the next DFA state for our input.
                    state = state.nextState(line.charAt(i));

                    // Checkpoint the state if it's accepting.
                    if (state.accept) {
                        rollbackMarker = i + 1;
                        rollbackState = state;
                    }

                    if (state == ERRSTATE || i == line.length() - 1) {
                        if (rollbackState != null) {

                            inBlockComment = rollbackState == BLOCK_COMMENT || rollbackState == BLOCK_STAR;

                            // We may want to ignore certain accepting states, such as whitespace.
                            if (rollbackState.kind != null) {
                                tokenList.add(new Token(line.substring(0, rollbackMarker), rollbackState.kind));
                            }
                        } else {
                            System.err.println("Error: no scanned token on line at " + i);
                            System.err.println(line);
                            System.exit(42);
                        }
                        break;
                    }

                }

                // Cut the lexeme off the line, and rescan it.
                line = line.substring(rollbackMarker);
            }
        }
        return tokenList;
    }
}
