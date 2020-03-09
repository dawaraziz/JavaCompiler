package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.statements.Statement;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;

import java.util.ArrayList;

public class MethodScope extends Scope {
    public final ArrayList<String> modifiers;
    public final ArrayList<Parameter> parameters;
    public final Statement body;

    MethodScope(final ASTHead method, final ClassScope classScope) {
        this.ast = method;
        this.parentScope = classScope;
        this.name = method.getMethodName();
        this.type = method.getMethodReturnType();

        modifiers = method.getMethodModifiers().get(0);
        parameters = method.getMethodParameters();
        body = Statement.generateStatementScope(method.getMethodBlock(), this);
    }

    MethodScope(final String name, final Type type, final ArrayList<String> modifiers,
                final ArrayList<Parameter> parameters) {
        this.ast = null;
        this.parentScope = null;
        this.name = name;

        this.type = type;
        this.modifiers = modifiers;
        this.parameters = parameters;
        this.body = null;
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

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        if (parameters == null) return false;

        for (final Parameter parameter : parameters) {
            if (parameter.name.getSimpleName().equals(variableName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        type.linkType(rootClass);

        if (parameters == null) return;

        parameters.forEach(c -> c.linkType(rootClass));
    }

    @Override
    public void checkTypeSoundness() {
        body.checkTypeSoundness();
    }

    public boolean checkIdentifierAgainstParameters(final String identifier) {
        return parameters.stream()
                .anyMatch(c -> c.name.getSimpleName().equals(identifier));
    }

    boolean checkIdentifier(final String identifier) {
        return this.name.equals(identifier);
    }
}