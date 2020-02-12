package com.project.scanner;

import com.project.parser.structure.ParserSymbol;
import com.project.scanner.structure.Kind;
import com.project.scanner.structure.ScannerState;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static com.project.scanner.structure.ScannerDFA.BLOCK_COMMENT;
import static com.project.scanner.structure.ScannerDFA.BLOCK_STAR;
import static com.project.scanner.structure.ScannerDFA.ERRSTATE;
import static com.project.scanner.structure.ScannerDFA.START_STATE;

public class JavaScanner {

    public static ArrayList<ParserSymbol> tokenizeFile(final String fileName) {
        Scanner fileScanner = null;

        // Load the file into a string scanner.
        try {
            final File code = new File(fileName);
            fileScanner = new Scanner(code);
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
            System.exit(42);
        }

        final ArrayList<ParserSymbol> tokenList = new ArrayList<>();
        tokenList.add(new ParserSymbol(Kind.BOF, "BOF"));

        boolean inBlockComment = false;
        int lineCounter = 1;
        while (fileScanner.hasNextLine()) {

            String line = fileScanner.nextLine();
            while (!line.isEmpty()) {

                // Used to checkpoint last accepted state in DFA.
                int rollbackMarker = 0;
                ScannerState rollbackState = null;

                // If we are in a block quote, we want to stay in until we exit.
                ScannerState state = inBlockComment ? BLOCK_COMMENT : START_STATE;

                for (int i = 0; i < line.length(); ++i) {
                    final char c = line.charAt(i);

                    // Checks if all our input is within the ASCII range.
                    if (c > 128) {
                        System.err.println("Unicode character identified on line " + lineCounter + " at " + i);
                        System.err.println(line);
                        System.exit(42);
                    }

                    // Fetch the next DFA state for our input.
                    state = state.nextState(c);

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
                                tokenList.add(new ParserSymbol(rollbackState.kind, line.substring(0, rollbackMarker)));
                            }
                        } else {
                            System.err.println("Error: no scanned token on line " + lineCounter + " at " + i);
                            System.err.println(line);
                            System.exit(42);
                        }
                        break;
                    }

                }

                // Cut the lexeme off the line, and rescan it.
                line = line.substring(rollbackMarker);
            }

            ++lineCounter;
        }

        tokenList.add(new ParserSymbol(Kind.EOF, "EOF"));
        return tokenList;
    }
}
