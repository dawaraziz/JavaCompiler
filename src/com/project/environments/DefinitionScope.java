package com.project.environments;

import com.project.ast.ASTHead;
import com.project.environments.structure.Type;

public class DefinitionScope extends Scope {
    public final Type type;
    public final String name;
    public final ASTHead initialization;

    public DefinitionScope(final ASTHead ast, final Scope parentScope,
                           final Type type, final String name, final ASTHead initialization) {
        this.ast = ast;
        this.parentScope = parentScope;
        this.type = type;
        this.name = name;
        this.initialization = initialization;
    }

}
