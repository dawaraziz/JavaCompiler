package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.structure.Parameter;

import java.util.ArrayList;
import java.util.Objects;

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
        if (parameters == null) return false;

        for (final Parameter parameter : parameters) {
            if (parameter.name.getSimpleName().equals(variableName)) {
                return true;
            }
        }
        return false;
    }

    public void linkTypes() {
        if (!(parentScope instanceof ClassScope)) {
            System.err.println("Found constructor with non-class scope parent; aborting!");
            System.exit(42);
        }

        final ClassScope parent = (ClassScope) parentScope;

        if (parameters == null) return;

        for (final Parameter param : parameters) {
            param.linkType(parent);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ConstructorScope that = (ConstructorScope) o;

        if (this.parameters == null && that.parameters == null) return true;
        if (this.parameters == null || that.parameters == null) return false;

        if (this.parameters.size() != that.parameters.size()) return false;

        for (int i = 0; i < this.parameters.size(); ++i) {
            if (!this.parameters.get(i).equals(that.parameters.get(i))) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameters);
    }
}
