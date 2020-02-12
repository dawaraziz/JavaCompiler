package com.project.Weeders;

import com.project.parser.structure.ParserSymbol;

import java.util.ArrayList;

import static com.project.parser.structure.ParserSymbol.getStringList;

public class FieldModifierWeeder {
    public static void weed(final ParserSymbol parseTree) {
        final ArrayList<ParserSymbol> propertyDeclarations = parseTree.getChildrenWithLexeme("FIELDDECLARATION");

        for (final ParserSymbol propertyDeclaration : propertyDeclarations) {
            final ArrayList<ParserSymbol> modifiers = propertyDeclaration.getDirectChildrenWithLexeme("MODIFIERS");

            final ArrayList<ParserSymbol> methodModifiers = new ArrayList<>();
            for (final ParserSymbol modifier : modifiers) {
                methodModifiers.addAll(modifier.getLeafNodes());
            }

            final ArrayList<String> stringModifiers = getStringList(methodModifiers);

            if (stringModifiers.contains("final")) {
                System.err.println("Encountered a final field.");
                System.exit(42);
            }

            if (stringModifiers.contains("private")) {
                System.err.println("Encountered a private field.");
                System.exit(42);
            }

            if (!(stringModifiers.contains("public") || stringModifiers.contains("protected"))) {
                System.err.println("Encountered a package-private field.");
                System.exit(42);
            }

            if (stringModifiers.contains("public") && stringModifiers.contains("protected")) {
                System.err.println("Encountered a public and protected field.");
                System.exit(42);
            }
        }
    }
}
