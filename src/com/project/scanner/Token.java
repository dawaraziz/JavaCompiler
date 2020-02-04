package com.project.scanner;

public class Token {
    private String lexeme;
    private final Kind kind;

    Token(final String lexeme, final Kind kind) {
        this.lexeme = lexeme;
        this.kind = kind;
    }

    public String getLexeme() {
        return lexeme;
    }

    public Kind getKind() {
        return kind;
    }

    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }
}
