package com.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import static com.project.ScannerDFA.*;

public class Main {

    static ArrayList<Token> tokenList = new ArrayList<>();

    public static void main(String[] args) {
        Scanner fileScanner = null;

        try {
            File code = new File(args[0]);
            fileScanner = new Scanner(code);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(42);
        }

        boolean isBlockComment = false;
        String line = "";
        while (fileScanner.hasNextLine()) {
            if (line.isEmpty()) line = fileScanner.nextLine();

            State state = isBlockComment ? BLOCK_COMMENT : START_STATE;

            int rollbackMarker = 0;
            State rollbackState = null;
            char[] lineArr = line.toCharArray();
            for (int i = 0; i < lineArr.length; ++i) {
                state = state.nextState(lineArr[i]);

                if (state.accept) {
                    rollbackMarker = i + 1;
                    rollbackState = state;
                }

                if (state == ERRSTATE || i == line.length() - 1) {
                    if (rollbackState != null) {
                        isBlockComment = (rollbackState == BLOCK_COMMENT || rollbackState == BLOCK_STAR);

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

            line = line.substring(rollbackMarker);
        }
//        }

        for (Token token : tokenList) {
            System.out.println(token.lexeme + " " + token.kind.name());
        }

        System.exit(0);
    }
}