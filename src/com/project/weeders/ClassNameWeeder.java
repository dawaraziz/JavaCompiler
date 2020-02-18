package com.project.weeders;

import com.project.environments.ast.ASTHead;

import java.io.File;

public class ClassNameWeeder {
    public static void weed(final ASTHead astHead, final String filename) {

        // Strips the base name from the file argument.
        final File f = new File(filename);
        final String[] base = f.getName().split("\\.");

        if (base.length != 2) {
            System.err.println("File name is strange; definitely not CLASS_NAME.java.");
            System.exit(42);
        }

        final String baseFileName = base[0];
        final String baseExtension = base[1];

        if (!baseExtension.equals("java")) {
            System.err.println("File is not a .java file.");
            System.exit(42);
        }

        if (!baseFileName.equals(astHead.getExpectedFileName())) {
            System.err.println("File name does not match class name.");
            System.exit(42);
        }
    }
}
