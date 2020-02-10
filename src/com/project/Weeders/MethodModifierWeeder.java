package com.project.Weeders;

import com.project.ParseTree.ParseTree;

import java.util.ArrayList;

import static com.project.ParseTree.ParseTree.getStringList;

public class MethodModifierWeeder {
    public static void weed(final ParseTree parseTree) {
        ArrayList<ParseTree> methodDeclarations = parseTree.getChildrenWithLexeme("METHODHEADER");
        ArrayList<ParseTree> constructorDeclarations = parseTree.getChildrenWithLexeme("CONSTRUCTORDECLARATION");

        if (constructorDeclarations.isEmpty()) {
            ArrayList<ParseTree> classDeclarations = parseTree.getChildrenWithLexeme("CLASSDECLARATION");
            if (!classDeclarations.isEmpty()) {
                System.err.println("Encountered a class with no constructor.");
                System.exit(42);
            }
        }

        ArrayList<ParseTree> declarations = new ArrayList<>();
        declarations.addAll(methodDeclarations);
        declarations.addAll(constructorDeclarations);

        for (ParseTree constructor : constructorDeclarations) {
            ArrayList<ParseTree> modifiers = constructor.getDirectChildrenWithLexeme("MODIFIERS");

            ArrayList<ParseTree> methodModifiers = new ArrayList<>();
            for (ParseTree modifier : modifiers) {
                methodModifiers.addAll(modifier.getLeafNodes());
            }

            ArrayList<String> stringModifiers = getStringList(methodModifiers);

            if (stringModifiers.contains("static")
                    || stringModifiers.contains("abstract")
                    || stringModifiers.contains("final")
                    || stringModifiers.contains("native")) {
                System.err.println("Encountered abstract/final/native/static constructor.");
                System.exit(42);
            }
        }

        for (ParseTree methodDeclaration : declarations) {
            ArrayList<ParseTree> modifiers = methodDeclaration.getDirectChildrenWithLexeme("MODIFIERS");

            ArrayList<ParseTree> methodModifiers = new ArrayList<>();
            for (ParseTree modifier : modifiers) {
                methodModifiers.addAll(modifier.getLeafNodes());
            }

            ArrayList<String> stringModifiers = getStringList(methodModifiers);

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

            if (stringModifiers.contains("native") || stringModifiers.contains("abstract")) {
                ArrayList<ParseTree> blocks = methodDeclaration.parent.getChildrenWithLexeme("BLOCK");
                if (!blocks.isEmpty()) {
                    System.err.println("Encountered native or abstract method with body.");
                    System.exit(42);
                }
            }

            if (methodDeclaration.parent.getLexeme().equals("ABSTRACTMETHODDECLARATION")
                    && (stringModifiers.contains("native")
                    || stringModifiers.contains("final")
                    || stringModifiers.contains("static"))) {
                System.err.println("Encountered final/static/native method in interface body.");
                System.exit(42);
            }
        }
    }
}