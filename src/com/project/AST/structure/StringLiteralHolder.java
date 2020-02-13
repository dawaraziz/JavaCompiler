package com.project.AST.structure;

import com.project.AST.ASTNode;

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
