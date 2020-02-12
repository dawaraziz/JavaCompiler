package com.project.AST;

import com.project.parser.structure.ParserSymbol;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;

class ASTNode {
    final Kind kind;
    final String lexeme;
    ASTNode parent;
    ArrayList<ASTNode> children = new ArrayList<>();

    ASTNode(final ParserSymbol symbol) {
        this.kind = symbol.kind;
        this.lexeme = symbol.lexeme;
    }

    ArrayList<ASTNode> findNodesWithLexeme(final String... lexemes) {
        final ArrayList<ASTNode> nodesWithLexeme = new ArrayList<>();

        for (final String lexeme : lexemes) {
            if (lexeme.equals(this.lexeme)) {
                nodesWithLexeme.add(this);
            }
        }

        for (final ASTNode child : children) {
            nodesWithLexeme.addAll(child.findNodesWithLexeme(lexemes));
        }

        return nodesWithLexeme;
    }

    ArrayList<ASTNode> getDirectChildrenWithKinds(final String... kinds) {
        final ArrayList<ASTNode> childrenWithKind = new ArrayList<>();

        for (final ASTNode child : children) {
            for (final String kind : kinds) {
                if (child.kind != null && kind != null && kind.equals(child.kind.toString())) {
                    childrenWithKind.add(child);
                }
            }
        }

        return childrenWithKind;
    }

    ArrayList<ASTNode> getDirectChildrenWithLexemes(final String... lexemes) {
        final ArrayList<ASTNode> childrenWithLexemes = new ArrayList<>();

        for (final ASTNode child : children) {
            for (final String lexeme : lexemes) {
                if (lexeme.equals(child.lexeme)) {
                    childrenWithLexemes.add(child);
                }
            }
        }

        return childrenWithLexemes;
    }

    ArrayList<ASTNode> getLeafNodes() {
        final ArrayList<ASTNode> leafNodes = new ArrayList<>();

        if (children.isEmpty()) {
            leafNodes.add(this);
        } else {
            for (final ASTNode child : children) {
                leafNodes.addAll(child.getLeafNodes());
            }
        }

        return leafNodes;
    }

    static ArrayList<String> lexemesToStringList(final ArrayList<ASTNode> nodes) {
        final ArrayList<String> strings = new ArrayList<>();

        for (final ASTNode node : nodes) {
            strings.add(node.lexeme);
        }

        return strings;
    }
}
