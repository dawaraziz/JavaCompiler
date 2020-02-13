package com.project;

import com.project.AST.ASTHead;
import com.project.Weeders.AbstractMethodWeeder;
import com.project.Weeders.ClassModifierWeeder;
import com.project.Weeders.ClassNameWeeder;
import com.project.Weeders.FieldModifierWeeder;
import com.project.Weeders.LiteralWeeder;
import com.project.Weeders.MethodModifierWeeder;
import com.project.parser.JavaParser;
import com.project.parser.structure.ParserSymbol;
import com.project.scanner.JavaScanner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(final String[] args) {

        if (args.length < 1) {
            System.err.println("No argument passed; expected file name.");
            System.exit(42);
        }

        final ArrayList<ParserSymbol> tokens = JavaScanner.tokenizeFile(args[0]);

        printTokens(tokens);

        final ParserSymbol parseTree;

        InputStream inputStreamJLR1 = Main.class.getResourceAsStream("/output.jlr1");
        if (inputStreamJLR1 == null) {
            inputStreamJLR1 = Main.class.getResourceAsStream("./output.jlr1");
        }
        parseTree = JavaParser.parseTokenList(tokens, new Scanner(inputStreamJLR1));

        final ASTHead AST = new ASTHead(parseTree);

        LiteralWeeder.weed(AST);
        AbstractMethodWeeder.weed(AST);
        ClassModifierWeeder.weed(AST);
        MethodModifierWeeder.weed(AST);
        FieldModifierWeeder.weed(AST);
        ClassNameWeeder.weed(AST, args[0]);

        System.exit(0);
    }

    private static void printTokens(final ArrayList<ParserSymbol> tokens) {
        System.out.println("Scanned Symbols:");
        for (final ParserSymbol token : tokens) {
            System.out.println(token.kind + ": " + token.lexeme);
        }
        System.out.println("-----------------------------------");
    }
}

