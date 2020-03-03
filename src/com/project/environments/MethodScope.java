package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.structure.Name;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;

import java.lang.reflect.Method;
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

    MethodScope(final String name, final Type type, final ArrayList<String> modifiers,
                final ArrayList<Parameter> parameters) {
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
        this.parameters = parameters;

        ast = null;
        bodyBlock = null;
        startScope = null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MethodScope that = (MethodScope) o;

        if (!this.name.equals(that.name)) return false;

        if (this.parameters == null && that.parameters == null) return true;

        if (this.parameters == null || that.parameters == null) return false;
        if (this.parameters.size() != that.parameters.size()) return false;
        for (int i = 0; i < this.parameters.size(); ++i) {
            if (!this.parameters.get(i).equals(that.parameters.get(i))) return false;
        }

        return true;
    }

    public boolean sameSignature(final MethodScope otherMethod) {
        if (this.parameters != null && otherMethod.parameters != null) {
            return (this.name.equals(otherMethod.name) && this.parameters.size() == otherMethod.parameters.size()
                    && this.parameters.containsAll(otherMethod.parameters)
                    && otherMethod.parameters.containsAll(this.parameters));
        }
        else if (this.parameters == null && otherMethod.parameters == null) {
            return (this.name.equals(otherMethod.name));
        }
        else return false;
    }

    @Override
    boolean isInitCheck(final String variableName) {
        if (parameters == null) return false;

        for (final Parameter parameter : parameters) {
            if (parameter.name.getSimpleName().equals(variableName)) {
                return true;
            }
        }

        return false;
    }

    void linkParameters() {
        if (!(parentScope instanceof ClassScope)) {
            System.err.println("Found method with non-class scope parent; aborting!");
            System.exit(42);
        }
        final ClassScope parent = (ClassScope) parentScope;

        if (type == null) {
            System.err.println("Found method with no type; aborting!");
            System.exit(42);
        }
        type.linkType(parent);

        if (parameters == null) return;

        for (final Parameter param : parameters) {
            param.linkType(parent);
        }
    }
}
