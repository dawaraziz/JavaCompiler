package com.project.Weeders;

import com.project.parser.structure.ParserSymbol;

import java.util.ArrayList;

import static com.project.parser.structure.ParserSymbol.getStringList;

public class MethodModifierWeeder {
    public static void weed(final ParserSymbol parseTree) {
        final ArrayList<ParserSymbol> methodDeclarations = parseTree.getChildrenWithLexeme("METHODHEADER");
        final ArrayList<ParserSymbol> constructorDeclarations = parseTree.getChildrenWithLexeme("CONSTRUCTORDECLARATION");

        if (constructorDeclarations.isEmpty()) {
            final ArrayList<ParserSymbol> classDeclarations = parseTree.getChildrenWithLexeme("CLASSDECLARATION");
            if (!classDeclarations.isEmpty()) {
                System.err.println("Encountered a class with no constructor.");
                System.exit(42);
            }
        }

        final ArrayList<ParserSymbol> declarations = new ArrayList<>();
        declarations.addAll(methodDeclarations);
        declarations.addAll(constructorDeclarations);

        for (final ParserSymbol constructor : constructorDeclarations) {
            final ArrayList<ParserSymbol> modifiers = constructor.getDirectChildrenWithLexeme("MODIFIERS");

            final ArrayList<ParserSymbol> methodModifiers = new ArrayList<>();
            for (final ParserSymbol modifier : modifiers) {
                methodModifiers.addAll(modifier.getLeafNodes());
            }

            final ArrayList<String> stringModifiers = getStringList(methodModifiers);

            if (stringModifiers.contains("static")
                    || stringModifiers.contains("abstract")
                    || stringModifiers.contains("final")
                    || stringModifiers.contains("native")) {
                System.err.println("Encountered abstract/final/native/static constructor.");
                System.exit(42);
            }
        }

        for (final ParserSymbol methodDeclaration : declarations) {
            final ArrayList<ParserSymbol> modifiers = methodDeclaration.getDirectChildrenWithLexeme("MODIFIERS");

            final ArrayList<ParserSymbol> methodModifiers = new ArrayList<>();
            for (final ParserSymbol modifier : modifiers) {
                methodModifiers.addAll(modifier.getLeafNodes());
            }

            final ArrayList<String> stringModifiers = getStringList(methodModifiers);

            if (stringModifiers.contains("abstract") &&
                    (stringModifiers.contains("final") || stringModifiers.contains("static"))) {
                System.err.println("Encountered an abstract and final/static method.");
                System.exit(42);
            } else if (stringModifiers.contains("static") && stringModifiers.contains("final")) {
                System.err.println("Encountered a static and final method.");
                System.exit(42);
            } else if (stringModifiers.contains("native") && !stringModifiers.contains("static")) {
                System.err.println("Encountered non-static native method.");
                System.exit(42);
            }

            if (!(stringModifiers.contains("public") || stringModifiers.contains("protected"))) {
                System.err.println("Encountered package private method.");
                System.exit(42);
            }

            if (stringModifiers.contains("private")) {
                System.err.println("Encountered a private method.");
                System.exit(42);
            }

            if (stringModifiers.contains("native") || stringModifiers.contains("abstract")) {
                final ArrayList<ParserSymbol> blocks = methodDeclaration.parent.getChildrenWithLexeme("BLOCK");
                if (!blocks.isEmpty()) {
                    System.err.println("Encountered native or abstract method with body.");
                    System.exit(42);
                }
            }

            if (methodDeclaration.parent.lexeme.equals("ABSTRACTMETHODDECLARATION")
                    && (stringModifiers.contains("native")
                    || stringModifiers.contains("final")
                    || stringModifiers.contains("static"))) {
                System.err.println("Encountered final/static/native method in interface body.");
                System.exit(42);
            }
        }
    }
}