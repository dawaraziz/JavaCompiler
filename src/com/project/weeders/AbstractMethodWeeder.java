package com.project.weeders;

import com.project.environments.ast.ASTHead;

public class AbstractMethodWeeder {
    public static void weed(final ASTHead astHead) {
        if (astHead.isAbstractMethodInitialized()) {
            System.err.println("Encountered initialized abstract method.");
            System.exit(42);
        }
    }
}
