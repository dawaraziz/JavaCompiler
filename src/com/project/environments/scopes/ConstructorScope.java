package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.statements.Statement;
import com.project.environments.structure.Parameter;

import java.util.ArrayList;
import java.util.Objects;

public class ConstructorScope extends Scope {
    public final ArrayList<String> modifiers;
    public final ArrayList<Parameter> parameters;
    public final Statement body;

    ConstructorScope(final ASTHead constructor, final ClassScope classScope) {
        this.ast = constructor;
        this.parentScope = classScope;
        this.name = constructor.getConstructorName();
        this.type = classScope.type;

        modifiers = constructor.getConstructorModifiers().get(0);
        parameters = constructor.getMethodParameters();
        body = Statement.generateStatementScope(constructor.getConstructorBlock(), this);
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        if (parameters == null) return false;

        for (final Parameter parameter : parameters) {
            if (parameter.name.equals(variableName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        if (parameters == null) return;

        parameters.forEach(c -> c.linkType(rootClass));
    }

    @Override
    public void checkTypeSoundness() {
        body.checkTypeSoundness();
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

    public void assignReachability() {
        if (body != null) body.assignReachability();
    }

    public void checkReachability() {
        if (body != null) body.checkReachability();
    }
}
