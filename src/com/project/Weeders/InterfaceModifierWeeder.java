package com.project.Weeders;

import com.project.AST.ASTSymbol;

import java.util.ArrayList;

import static com.project.AST.ASTKinds.INTERFACE_MODIFIERS;

public class InterfaceModifierWeeder {
    public static void weed(ASTSymbol currentSymbol) {
        if (currentSymbol.type == INTERFACE_MODIFIERS) {
            ArrayList<String> modifiers = new ArrayList<>();

            for (ASTSymbol symbol : currentSymbol.children) {
                modifiers.add(symbol.lexeme);
            }

            if (modifiers.contains("abstract")
                    && (modifiers.contains("static") || modifiers.contains("final"))) {
                System.err.println("Encountered an abstract and static/final class.");
                System.exit(42);
            } else if (modifiers.contains("static") && modifiers.contains("final")) {
                System.err.println("Encountered an final and static class.");
                System.exit(42);
            } else if (modifiers.contains("native") && !modifiers.contains("static")) {
                System.err.println("Encountered a non-static native class.");
                System.exit(42);
            }
        } else {
            for (ASTSymbol symbol : currentSymbol.children) {
                weed(symbol);
            }
        }
    }
}
