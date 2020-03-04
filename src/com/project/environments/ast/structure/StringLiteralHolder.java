package com.project.environments.ast.structure;

import com.project.environments.ast.ASTNode;

public class StringLiteralHolder {
    private final ASTNode node;
    public final String value;

    public StringLiteralHolder(final ASTNode node, final String value) {
        this.node = node;
        this.value = value;
    }

    public void setNodeLexeme(final String lexeme) {
        node.setLexeme(lexeme);
    }
}
