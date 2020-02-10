package com.project.Weeders;

import com.project.ParseTree.ParseTree;

import java.util.ArrayList;

public class ClassNameWeeder {
    public static void weed(final ParseTree parseTree, String[] args) {

        // Strips the base name from the file argument.
        String filename = args[0];
        String[] splitFilename = filename.split("[\\\\/]");
        String base = splitFilename[splitFilename.length - 1];
        String[] splitBase = base.split("\\.");
        if (splitBase.length != 2) {
            System.err.println("File name is strange; definitely not CLASS_NAME.java.");
            System.exit(42);
        }
        String baseFilename = splitBase[0];
        String basePostfix = splitBase[1];

        if (!basePostfix.equals("java")) {
            System.err.println("File is not a .java file.");
            System.exit(42);
        }

        ArrayList<ParseTree> classDeclarations = parseTree.getChildrenWithLexeme("CLASSDECLARATION");
        for (ParseTree classDeclaration : classDeclarations) {
            ArrayList<ParseTree> className = classDeclaration.getDirectChildrenWithLexeme(baseFilename);
            if (className.isEmpty()) {
                System.err.println("File name does not match class name.");
                System.exit(42);
            }
        }
    }
}
