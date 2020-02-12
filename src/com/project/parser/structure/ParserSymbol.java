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
}
