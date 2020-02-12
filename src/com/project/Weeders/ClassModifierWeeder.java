package com.project.Weeders;

import com.project.parser.structure.ParserSymbol;

import java.util.ArrayList;

import static com.project.parser.structure.ParserSymbol.getStringList;

public class ClassModifierWeeder {
    public static void weed(final ParserSymbol parseTree) {
        final ArrayList<ParserSymbol> classDeclarations = parseTree.getChildrenWithLexeme("CLASSDECLARATION");
        for (final ParserSymbol classDeclaration : classDeclarations) {
            final ArrayList<ParserSymbol> modifiers = classDeclaration.getDirectChildrenWithLexeme("MODIFIERS");

            final ArrayList<ParserSymbol> classModifiers = new ArrayList<>();
            for (final ParserSymbol modifier : modifiers) {
                classModifiers.addAll(modifier.getLeafNodes());
            }

            final ArrayList<String> stringModifiers = getStringList(classModifiers);

            if (stringModifiers.contains("abstract") && stringModifiers.contains("final")) {
                System.err.println("Encountered an abstract and final class.");
                System.exit(42);
            } else if (stringModifiers.contains("static")
                    || stringModifiers.contains("protected")
                    || stringModifiers.contains("native")) {
                System.err.println("Encountered an invalid modifier static/protected/native on a class.");
                System.exit(42);
            } else if (!stringModifiers.contains("public")) {
                System.err.println("Encountered non-public class.");
                System.exit(42);
            }
        }
    }
}
