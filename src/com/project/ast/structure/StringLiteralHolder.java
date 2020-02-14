package com.project.ast.structure;

import com.project.ast.ASTNode;

public class StringLiteralHolder {
    private final ASTNode node;
    public String value;

    public StringLiteralHolder(final ASTNode node, final String value) {
        this.node = node;
        this.value = value;
    }

    public void setNodeLexeme(final String lexeme) {
        node.setLexeme(lexeme);
    }
}
