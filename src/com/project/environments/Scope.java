package com.project.environments;

import com.project.ast.ASTHead;

public abstract class Scope {
    public String name;
    public ASTHead ast;
    public Scope parentScope;
}
