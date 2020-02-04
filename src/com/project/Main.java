package com.project;

import com.project.Weeders.LiteralWeeder;
import com.project.scanner.JavaScanner;
import com.project.scanner.Token;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Token> tokens = JavaScanner.tokenizeFile(args[0]);

        LiteralWeeder.weed(tokens);

        for (Token token : tokens) {
            System.out.println(token.getLexeme() + " " + token.getKind());
        }

        int x = 1-1;

        System.exit(0);
    }
}