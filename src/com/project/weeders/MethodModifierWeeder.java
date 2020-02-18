package com.project.weeders;

import com.project.environments.ast.ASTHead;

import java.util.ArrayList;

import static com.project.environments.ast.ASTHead.ABSTRACT;
import static com.project.environments.ast.ASTHead.FINAL;
import static com.project.environments.ast.ASTHead.NATIVE;
import static com.project.environments.ast.ASTHead.PROTECTED;
import static com.project.environments.ast.ASTHead.PUBLIC;
import static com.project.environments.ast.ASTHead.STATIC;

public class MethodModifierWeeder {
    public static void weed(final ASTHead astHead) {
        final ArrayList<ArrayList<String>> constructorModifiers = astHead.getConstructorModifiers();
        final boolean isInterface = astHead.isFileTypeInterface();

        if (astHead.getConstructorModifiers().isEmpty() && !isInterface) {
            System.err.println("Encountered a class with no constructor.");
            System.exit(42);
        }

        for (final ArrayList<String> modifiers : constructorModifiers) {
            if (modifiers.contains(STATIC) || modifiers.contains(ABSTRACT)
                    || modifiers.contains(FINAL) || modifiers.contains(NATIVE)) {
                System.err.println("Encountered invalid constructor modifier.");
                System.exit(42);
            } else if (modifiers.contains(PUBLIC) && modifiers.contains(PROTECTED)) {
                System.err.println("Encountered constructor with two access modifiers.");
                System.exit(42);
            } else if (!modifiers.contains(PUBLIC) && !modifiers.contains(PROTECTED)) {
                System.err.println("Encountered package private constructor.");
                System.exit(42);
            }
        }

        final ArrayList<ArrayList<String>> methodModifiers = astHead.getMethodModifiers();

        // If we're missing a modifier list, it must have no access modifier.
        if (methodModifiers.size() != astHead.getMethodCount()) {
            System.err.println("Encountered package private method.");
            System.exit(42);
        }

        for (final ArrayList<String> modifiers : methodModifiers) {
            if (isInterface && (modifiers.contains(NATIVE) || modifiers.contains(FINAL)
                    || modifiers.contains(STATIC))) {
                System.err.println("Encountered native, final, or static modifier on interface method.");
                System.exit(42);
            } else if (modifiers.contains(ABSTRACT) &&
                    (modifiers.contains(FINAL) || modifiers.contains(STATIC))) {
                System.err.println("Encountered an abstract and final/static method.");
                System.exit(42);
            } else if (modifiers.contains(STATIC) && modifiers.contains(FINAL)) {
                System.err.println("Encountered a static final method.");
                System.exit(42);
            } else if (modifiers.contains(NATIVE) && !modifiers.contains(STATIC)) {
                System.err.println("Encountered a non-static native method.");
                System.exit(42);
            } else if (!modifiers.contains(PUBLIC) && !modifiers.contains(PROTECTED)) {
                System.err.println("Encountered package private method.");
                System.exit(42);
            } else if (modifiers.contains(PUBLIC) && modifiers.contains(PROTECTED)) {
                System.err.println("Encountered two access modifiers on method.");
                System.exit(42);
            }
        }
    }
}