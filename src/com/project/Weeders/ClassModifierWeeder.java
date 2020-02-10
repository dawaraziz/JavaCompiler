package com.project.Weeders;

import com.project.ParseTree.ParseTree;

import java.util.ArrayList;

import static com.project.ParseTree.ParseTree.getStringList;

public class ClassModifierWeeder {
    public static void weed(final ParseTree parseTree) {
        ArrayList<ParseTree> classDeclarations = parseTree.getChildrenWithLexeme("CLASSDECLARATION");
        for (ParseTree classDeclaration : classDeclarations) {
            ArrayList<ParseTree> modifiers = classDeclaration.getDirectChildrenWithLexeme("MODIFIERS");

            ArrayList<ParseTree> classModifiers = new ArrayList<>();
            for (ParseTree modifier : modifiers) {
                classModifiers.addAll(modifier.getLeafNodes());
            }

            ArrayList<String> stringModifiers = getStringList(classModifiers);

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
