package com.project;

public class Token {
    String lexeme;
    Kind kind;

    public Token(String lexeme, Kind kind) {
        this.lexeme = lexeme;
        this.kind = kind;
    }
}
