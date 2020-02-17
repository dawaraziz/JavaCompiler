package com.project.environments;

import com.project.ast.ASTHead;

import java.util.ArrayList;

public class BlockScope extends Scope {
    public ArrayList<ASTHead> statements;
    public ArrayList<Scope> childScopes;

    public BlockScope(final ASTHead ast, final Scope parentScope) {
        this.ast = ast;
        this.parentScope = parentScope;
        statements = new ArrayList<>();
        childScopes = new ArrayList<>();
    }
}
