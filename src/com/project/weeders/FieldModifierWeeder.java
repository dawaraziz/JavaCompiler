package com.project.weeders;

import com.project.ast.ASTHead;

import java.util.ArrayList;

import static com.project.ast.ASTHead.FINAL;
import static com.project.ast.ASTHead.PROTECTED;
import static com.project.ast.ASTHead.PUBLIC;


public class FieldModifierWeeder {
    public static void weed(final ASTHead astHead) {
        final ArrayList<ArrayList<String>> fieldModifiers = astHead.getFieldModifiers();

        // If we're missing a modifier list, it must have no access modifier.
        if (fieldModifiers.size() != astHead.getFieldCount()) {
            System.err.println("Encountered package private field.");
            System.exit(42);
        }

        for (final ArrayList<String> modifiers : fieldModifiers) {
            if (modifiers.contains(FINAL)) {
                System.err.println("Encountered a final field.");
                System.exit(42);
            } else if (!modifiers.contains(PROTECTED) && !modifiers.contains(PUBLIC)) {
                System.err.println("Encountered a package-private field.");
                System.exit(42);
            }
        }
    }
}
