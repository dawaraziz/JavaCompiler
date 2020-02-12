package com.project.Weeders;

import com.project.parser.structure.ParserSymbol;

import java.util.ArrayList;

public class ClassNameWeeder {
    public static void weed(final ParserSymbol parseTree, final String[] args) {

        // Strips the base name from the file argument.
        final String filename = args[0];
        final String[] splitFilename = filename.split("[\\\\/]");
        final String base = splitFilename[splitFilename.length - 1];
        final String[] splitBase = base.split("\\.");
        if (splitBase.length != 2) {
            System.err.println("File name is strange; definitely not CLASS_NAME.java.");
            System.exit(42);
        }
        final String baseFilename = splitBase[0];
        final String basePostfix = splitBase[1];

        if (!basePostfix.equals("java")) {
            System.err.println("File is not a .java file.");
            System.exit(42);
        }

        final ArrayList<ParserSymbol> declarations = parseTree.getChildrenWithLexeme("CLASSDECLARATION");
        declarations.addAll(parseTree.getChildrenWithLexeme("INTERFACEDECLARATION"));
        for (final ParserSymbol classDeclaration : declarations) {
            final ArrayList<ParserSymbol> className = classDeclaration.getDirectChildrenWithLexeme(baseFilename);
            if (className.isEmpty()) {
                System.err.println("File name does not match class name.");
                System.exit(42);
            }
        }
    }
}
