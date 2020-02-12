package com.project.Weeders;

import com.project.AST.ASTHead;

public class AbstractMethodWeeder {
    public static void weed(final ASTHead astHead) {
        if (astHead.isAbstractMethodInitialized()) {
            System.err.println("Encountered initialized abstract method.");
            System.exit(42);
        }
    }
}
