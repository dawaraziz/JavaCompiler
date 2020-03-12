package com.project.environments.ast;

import com.project.parser.structure.ParserSymbol;
import com.project.scanner.structure.Kind;
import resources.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Stack;

public class ASTNode {
    public Kind kind;
    public String lexeme;
    public ASTNode parent;
    public final ArrayList<ASTNode> children = new ArrayList<>();

    ASTNode(final ParserSymbol symbol) {
        this.kind = symbol.kind;
        this.lexeme = symbol.lexeme;
    }

    ASTNode(final Kind kind, final String lexeme) {
        this.kind = kind;
        this.lexeme = lexeme;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ASTNode astNode = (ASTNode) o;
        return kind == astNode.kind &&
                Objects.equals(lexeme, astNode.lexeme) &&
                Objects.equals(parent, astNode.parent) &&
                Objects.equals(children, astNode.children);
    }

    public String toString() {
        return kind != null ? kind.toString() : lexeme;
    }

    boolean withinLexeme(final String lex) {
        ASTNode node = this;
        while (node.parent != null) {
            if (node.parent.lexeme.equals(lex)) {
                return true;
            }
            node = node.parent;
        }
        return false;
    }

    public boolean within(final String lex) {
        if (parent == null) return false;

        if (parent.lexeme.equals(lex)) {
            return true;
        } else {
            return parent.within(lex);
        }
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

    public ASTNode findFirstDirectChildNodeWithLexeme(final String lexeme) {
        for (final ASTNode child : children) {
            if (child.lexeme.equals(lexeme)){
                return child;
            }
        }
        return null;
    }

    public ASTNode findFirstDirectChildNodeWithKind(final Kind kind) {
        for (final ASTNode child : children) {
            if (child.kind.equals(kind)){
                return child;
            }
        }
        return null;
    }

    // Once we find a direct child with a kind 'afterKind' we will recurse its siblings after for any of kind target
    public ArrayList<ASTNode> findChildKindsAfterNodeWithKind(final Kind target, final Kind afterKind) {
        ArrayList<ASTNode> nodes = new ArrayList();
        boolean found = false;
        for (int i = children.size()-1; i >= 0; i--) {
            ASTNode child = children.get(i);
            //Once we have found a direct child with the kind we can search recursively for the target nodes
            if (child.kind == afterKind){
                found = true;
            }
            if (found){
                // Recurse through this child to find targets
                nodes.addAll(child.findNodesWithKinds(target));
            }
        }
        return nodes;
    }

    public ArrayList<ASTNode> findChildKindsAfterNodeWithLexeme(final Kind target, final String afterLex) {
        ArrayList<ASTNode> nodes = new ArrayList();
        boolean found = false;
        for (int i = children.size()-1; i >= 0; i--) {
            ASTNode child = children.get(i);
            //Once we have found a direct child with the kind we can search recursively for the target nodes
            if (child.lexeme.equals(afterLex)){
                found = true;
            }
            if (found){
                // Recurse through this child to find targets
                nodes.addAll(child.findNodesWithKinds(target));
            }
        }
        return nodes;
    }

    // Once we find a direct child with a kind 'afterKind' we will return the next child after it
    public ASTNode findFirstChildAfterChildWithKind(final Kind target) {
        boolean found = false;
        for (int i = children.size()-1; i >= 0; i--) {
            ASTNode child = children.get(i);
            if (found){
                return child;
            }
            //Once we have found a direct child with the kind we can search recursively for the target nodes
            if (child.kind == target){
                found = true;
            }
        }
        return null;
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

    String stringFromChildren() {
        final StringBuilder sb = new StringBuilder();

        for (final ASTNode node : this.children) {
            sb.insert(0, node.lexeme);
        }

        return sb.toString();
    }


    public void setLexeme(final String lexeme) {
        this.lexeme = lexeme;
    }

    public boolean parentOneOfLexeme(final String... lexemes){
        for (String lex : lexemes){
            if (this.parent.lexeme.equals(lex)){
                return true;
            }
        }
        return false;
    }




    public void printAST() {
        final Stack<Pair<ASTNode, Integer>> stack = new Stack<>();
        stack.add(new Pair<>(this, 0));
        while (!stack.empty()) {
            final Pair<ASTNode, Integer> pair = stack.pop();
            final ASTNode curr = pair.getO1();
            final int level = pair.getO2();
            for (int i = 0; i < level; i++) {
                System.out.print("\t");
            }
            System.out.println(curr.lexeme + " : " + curr.kind + " : " + level);
            for (final ASTNode child : curr.children) {
                stack.add(new Pair<>(child, level + 1));
            }
        }
    }
}
