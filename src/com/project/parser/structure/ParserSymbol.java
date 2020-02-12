package com.project.parser.structure;

import com.project.scanner.structure.Kind;

import java.util.ArrayList;

public class ParserSymbol {

    public Kind kind;
    public String lexeme;
    public ParserSymbol parent;
    public ArrayList<ParserSymbol> children = new ArrayList<>();

    public ParserSymbol(final Kind kind, final String lexeme) {
        this.kind = kind;
        this.lexeme = lexeme;
        this.parent = null;
    }

    public boolean equals(final ParserSymbol other) {
        return (other != null) &&
                ((this.kind != null && other.kind != null && this.kind == other.kind)
                        || (this.lexeme != null && other.lexeme != null && this.lexeme.equalsIgnoreCase(other.lexeme)));
    }

    public boolean equals(final String other) {
        return this.lexeme.equalsIgnoreCase(other);
    }

    public String toString() {
        return kind != null ? kind.toString() : lexeme;
    }

    public ArrayList<ParserSymbol> getChildrenWithLexeme(final String lexeme) {
        final ArrayList<ParserSymbol> childrenWithLexeme = new ArrayList<>();

        for (final ParserSymbol child : children) {
            childrenWithLexeme.addAll(child.getChildrenWithLexeme(lexeme));
            if (child.lexeme.equals(lexeme)) {
                childrenWithLexeme.add(child);
            }
        }

        return childrenWithLexeme;
    }

    public ArrayList<ParserSymbol> getDirectChildrenWithLexeme(final String lexeme) {
        final ArrayList<ParserSymbol> childrenWithLexeme = new ArrayList<>();

        for (final ParserSymbol child : children) {
            if (child.lexeme.equals(lexeme)) {
                childrenWithLexeme.add(child);
            }
        }

        return childrenWithLexeme;
    }

    public static ArrayList<String> getStringList(final ArrayList<ParserSymbol> symbolList) {
        final ArrayList<String> stringList = new ArrayList<>();

        for (final ParserSymbol symbol : symbolList) {
            stringList.add(symbol.lexeme);
        }

        return stringList;
    }

    public ArrayList<ParserSymbol> getLeafNodes() {
        final ArrayList<ParserSymbol> leafNodes = new ArrayList<>();

        if (children.isEmpty()) {
            leafNodes.add(this);
        } else {
            for (final ParserSymbol child : children) {
                leafNodes.addAll(child.getLeafNodes());
            }
        }

        return leafNodes;
    }
}
