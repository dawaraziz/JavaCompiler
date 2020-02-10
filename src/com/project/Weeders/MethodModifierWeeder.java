package com.project.Weeders;

import com.project.AST.ASTSymbol;

import java.util.ArrayList;

import static com.project.AST.ASTKinds.CLASS_MODIFIERS;

public class MethodModifierWeeder {
    public static void weed(ASTSymbol currentSymbol) {
        if (currentSymbol.type == CLASS_MODIFIERS) {
            ArrayList<String> modifiers = new ArrayList<>();

            for (ASTSymbol symbol : currentSymbol.children) {
                modifiers.add(symbol.lexeme);
            }

            if (modifiers.contains("abstract") && modifiers.contains("final")) {
                System.err.println("Encountered a final and abstract class.");
                System.exit(42);
            }
        } else {
            for (ASTSymbol symbol : currentSymbol.children) {
                weed(symbol);
            }
        }
    }
}
