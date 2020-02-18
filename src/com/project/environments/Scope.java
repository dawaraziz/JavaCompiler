package com.project.environments;

import com.project.environments.ast.ASTHead;

public abstract class Scope {
    public String name;
    public ASTHead ast;
    public Scope parentScope;

    abstract boolean isInitCheck(final String variableName);
}
