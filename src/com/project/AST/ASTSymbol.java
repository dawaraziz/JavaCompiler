package com.project.AST;

import java.util.ArrayList;

public class ASTSymbol {
    public ASTKinds type;
    public String lexeme;

    public ArrayList<ASTSymbol> children = new ArrayList<>();
}
