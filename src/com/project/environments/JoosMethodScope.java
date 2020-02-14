package com.project.environments;

import com.project.ast.ASTHead;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;

import java.util.ArrayList;

public class JoosMethodScope extends Scope {
    public final String name;
    public final ASTHead ast;

    public final Type type;

    public final ArrayList<String> modifiers;
    public final ArrayList<Parameter> parameters;
    public final ASTHead bodyBlock;

    JoosMethodScope(final ASTHead method, final JoosClassScope joosClassScope) {
        bodyBlock = method.getMethodBlock();
        modifiers = method.getMethodModifiers().get(0);
        parameters = method.getMethodParameters();
        parentScope = joosClassScope;

        type = method.getMethodReturnType();
        name = method.getMethodName();
        ast = method;
    }
}
