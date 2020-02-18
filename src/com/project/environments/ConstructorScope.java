package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.structure.Parameter;

import java.util.ArrayList;

public class ConstructorScope extends Scope {
    public final String name;
    public final ASTHead ast;

    public final ArrayList<String> modifiers;
    public final ArrayList<Parameter> parameters;
    public final ASTHead bodyBlock;

    public final BlockScope startScope;

    ConstructorScope(final ASTHead constructor, final ClassScope classScope) {
        bodyBlock = constructor.getConstructorBlock();
        modifiers = constructor.getConstructorModifiers().get(0);
        parameters = constructor.getMethodParameters();
        parentScope = classScope;

        name = constructor.getConstructorName();
        ast = constructor;

        if (bodyBlock != null) {
            startScope = new BlockScope(bodyBlock, this);
        } else {
            startScope = null;
        }
    }

    @Override
    boolean isInitCheck(final String variableName) {
        for (final Parameter parameter : parameters) {
            if (parameter.name.getSimpleName().equals(variableName)) {
                return true;
            }
        }
        return false;
    }
}
