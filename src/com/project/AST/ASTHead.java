package com.project.AST;

import com.project.parser.structure.ParserSymbol;

import java.util.ArrayList;

public class ASTHead {
    public ASTNode headNode;

    public ASTHead(final ParserSymbol parseTree) {
        headNode = trimParseTree(parseTree, null);
    }

    public static ASTNode trimParseTree(ParserSymbol parseTree, final ASTNode parent) {
        if (parseTree.kind == null && parseTree.children.isEmpty()) {
            System.err.println("Got a terminal symbol with no children; aborting!");
            System.exit(42);
        }

        // Cull any transitional nodes.
        while (parseTree.children.size() == 1) {
            parseTree = parseTree.children.get(0);
        }

        final ASTNode node = new ASTNode(parseTree);
        node.parent = parent;

        // Cull any recursively expanding nodes.
        for (boolean culled = false; ; culled = false) {

            for (int i = 0; i < parseTree.children.size(); ++i) {
                final ParserSymbol child = parseTree.children.get(i);
                if (child.equals(parseTree)) {

                    // If we find a child we think could be culled, we have to be very careful.
                    // We need to preserve the ordering, so we make a new list, expand the problem
                    // symbol at the correct place, and add all the other children back in.
                    final ArrayList<ParserSymbol> culledChildren = new ArrayList<>();
                    //noinspection SuspiciousListRemoveInLoop
                    parseTree.children.remove(i);

                    for (int j = 0; j < i; ++j) {
                        culledChildren.add(parseTree.children.get(j));
                    }

                    culledChildren.addAll(child.children);

                    for (int j = i; j < parseTree.children.size(); ++j) {
                        culledChildren.add(parseTree.children.get(j));
                    }

                    culled = true;

                    parseTree.children = culledChildren;
                }
            }

            // We want to do this as many times as required, so we need to
            // check the exit condition based on whether we've culled or not.
            // We can't move this into the for because it assigns culled first.
            if (!culled) break;
        }

        // Trim all of our children.
        for (final ParserSymbol child : parseTree.children) {
            node.children.add(trimParseTree(child, node));
        }

        return node;
    }
}
