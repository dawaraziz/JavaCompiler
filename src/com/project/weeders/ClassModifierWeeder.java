package com.project.weeders;

import com.project.environments.ast.ASTHead;

import java.util.ArrayList;

import static com.project.environments.ast.ASTHead.ABSTRACT;
import static com.project.environments.ast.ASTHead.FINAL;
import static com.project.environments.ast.ASTHead.NATIVE;
import static com.project.environments.ast.ASTHead.PROTECTED;
import static com.project.environments.ast.ASTHead.PUBLIC;
import static com.project.environments.ast.ASTHead.STATIC;

public class ClassModifierWeeder {
    public static void weed(final ASTHead astHead) {
        final ArrayList<String> classModifiers = astHead.getClassModifiers();

        if (classModifiers.contains(ABSTRACT) && classModifiers.contains(FINAL)) {
            System.err.println("Encountered an abstract and final class.");
            System.exit(42);
        } else if (classModifiers.contains(STATIC) || classModifiers.contains(PROTECTED)
                || classModifiers.contains(NATIVE)) {
            System.err.println("Encountered an invalid modifier on a class.");
            System.exit(42);
        } else if (!classModifiers.contains(PUBLIC)) {
            System.err.println("Encountered non-public class.");
            System.exit(42);
        }
    }
}
