package com.project.environments.ast;

import com.project.parser.structure.ParserSymbol;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;

public class ASTNode {
    public Kind kind;
    public String lexeme;
    public ASTNode parent;
    public ArrayList<ASTNode> children = new ArrayList<>();

    ASTNode(final ParserSymbol symbol) {
        this.kind = symbol.kind;
        this.lexeme = symbol.lexeme;
    }

    public String toString() {
        return kind != null ? kind.toString() : lexeme;
    }

    public ArrayList<ASTNode> findNodesWithLexeme(final String... lexemes) {
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

    public ArrayList<ASTNode> findNodesWithKinds(final Kind... kinds) {
        final ArrayList<ASTNode> nodesWithkind = new ArrayList<>();

        for (final Kind kind : kinds) {
            if (kind.equals(this.kind)) {
                nodesWithkind.add(this);
            }
        }

        for (final ASTNode child : children) {
            nodesWithkind.addAll(child.findNodesWithKinds(kinds));
        }

        return nodesWithkind;
    }

    public ArrayList<ASTNode> getDirectChildrenWithKinds(final String... kinds) {
        final ArrayList<ASTNode> childrenWithKind = new ArrayList<>();

        for (final ASTNode child : children) {
            for (final String kind : kinds) {
                System.out.println(kind + " equals " + child.kind);
                if (child.kind != null && kind != null && kind.equals(child.kind.toString())) {
                    childrenWithKind.add(child);
                }
            }
        }

        return childrenWithKind;
    }

    public ArrayList<ASTNode> getDirectChildrenWithLexemes(final String... lexemes) {
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

    public ArrayList<ASTNode> getLeafNodes() {
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


    public static ArrayList<String> lexemesToStringList(final ArrayList<ASTNode> nodes) {
        final ArrayList<String> strings = new ArrayList<>();

        for (final ASTNode node : nodes) {
            strings.add(node.lexeme);
        }

        return strings;
    }


    public void setLexeme(final String lexeme) {
        this.lexeme = lexeme;
    }
}
