package com.project.Weeders;

import com.project.ParseTree.ParseTree;

import java.util.ArrayList;

import static com.project.ParseTree.ParseTree.getStringList;

public class FieldModifierWeeder {
    public static void weed(final ParseTree parseTree) {
        ArrayList<ParseTree> propertyDeclarations = parseTree.getChildrenWithLexeme("FIELDDECLARATION");

        for (ParseTree propertyDeclaration : propertyDeclarations) {
            ArrayList<ParseTree> modifiers = propertyDeclaration.getDirectChildrenWithLexeme("MODIFIERS");

            ArrayList<ParseTree> methodModifiers = new ArrayList<>();
            for (ParseTree modifier : modifiers) {
                methodModifiers.addAll(modifier.getLeafNodes());
            }

            ArrayList<String> stringModifiers = getStringList(methodModifiers);

            if (stringModifiers.contains("final")) {
                System.err.println("Encountered a final field.");
                System.exit(42);
            }
        }
    }
}
