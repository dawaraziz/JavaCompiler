package com.project;

import java.util.ArrayList;

import static com.project.ScannerDFA.ERRSTATE;
import static com.project.ScannerDFA.START_STATE;

public class Main {

    static ArrayList<Token> tokenList = new ArrayList<>();

    public static void main(String[] args) {
//        Scanner fileScanner = null;

//        try {
//            File code = new File(args[0]);
//            fileScanner = new Scanner(code);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            System.exit(42);
//        }

//        while (fileScanner.hasNextLine()) {
//            String line = fileScanner.nextLine();
        String line = "002";
        State state = START_STATE;

        while (!line.isEmpty()) {
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
                    processToken(line, rollbackMarker, rollbackState, i);
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

    private static void processToken(String line, int rollbackMarker, State rollbackState, int i) {
        if (rollbackState != null) {
            tokenList.add(new Token(line.substring(0, rollbackMarker), rollbackState.kind));
        } else {
            System.err.println("Error: no scanned token on line at " + i);
            System.err.println(line);
            System.exit(42);
        }
    }
}