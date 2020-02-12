package com.project.AST;

import com.project.parser.structure.ParserSymbol;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;

public class ASTNode {
    public Kind kind;
    public String lexeme;
    public ASTNode parent;
    public ArrayList<ASTNode> children = new ArrayList<>();

    public ASTNode(ParserSymbol symbol) {
        this.kind = symbol.kind;
        this.lexeme = symbol.lexeme;
    }
}
