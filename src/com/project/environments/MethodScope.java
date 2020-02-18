package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;

import java.util.ArrayList;

public class MethodScope extends Scope {
    public final String name;
    public final ASTHead ast;

    public final Type type;

    public final ArrayList<String> modifiers;
    public final ArrayList<Parameter> parameters;
    public final ASTHead bodyBlock;

    public final BlockScope startScope;

    MethodScope(final ASTHead method, final ClassScope classScope) {
        bodyBlock = method.getMethodBlock();
        modifiers = method.getMethodModifiers().get(0);
        parameters = method.getMethodParameters();
        parentScope = classScope;

        type = method.getMethodReturnType();
        name = method.getMethodName();
        ast = method;

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
