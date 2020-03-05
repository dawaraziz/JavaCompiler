package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.structure.Type;

public abstract class Scope {
    public String name;
    public ASTHead ast;
    public Scope parentScope;
    public Type type;

    public abstract boolean isVariableNameFree(final String variableName);

    public abstract void linkTypesToQualifiedNames(final ClassScope rootClass);

    public abstract void checkTypeSoundness();
}
