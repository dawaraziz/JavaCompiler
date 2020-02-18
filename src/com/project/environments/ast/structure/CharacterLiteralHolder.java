package com.project.environments.ast.structure;

import com.project.environments.ast.ASTNode;

public class CharacterLiteralHolder {
    private final ASTNode node;
    public String value;

    public CharacterLiteralHolder(final ASTNode node, final String value) {
        this.node = node;
        this.value = value;
    }

    public void setNodeLexeme(final String lexeme) {
        node.setLexeme(lexeme);
    }
}
