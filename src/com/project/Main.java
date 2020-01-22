package com.project;

import com.project.scanner.JavaScanner;
import com.project.scanner.Token;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Token> tokens = JavaScanner.tokenizeFile(args[0]);
        
        for (Token token : tokens) {
            System.out.println(token.getLexeme() + " " + token.getKind());
        }

        System.exit(0);
    }
}