package com.project.environments.structure;

import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.ConstructorScope;
import com.project.environments.scopes.MethodScope;
import com.project.environments.scopes.Scope;

import java.util.ArrayList;

public class Parameter extends Scope {
    public Parameter(final Type type, final Name name, final Scope scope) {
        this.type = type;
        this.name = name.getSimpleName();
        this.parentScope = scope;
    }

    public Parameter(final Type type, final String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof Parameter)) {
            return false;
        } else if (obj == this) {
            return true;
        }

        return this.type.equals(((Parameter) obj).type);
    }

    public void linkType(final ClassScope classScope) {
        type.linkType(classScope);
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {

    }

    @Override
    public String toString() {
        return type.toString();
    }

    @Override
    public void checkTypeSoundness() {

    }

    @Override
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();

        int index = 12;
        if (parentScope instanceof MethodScope) {
            final MethodScope methodScope = (MethodScope) parentScope;

            for (final Parameter parameter : methodScope.parameters) {
                if (parameter.equals(this)) break;
                index += 4;
            }
        } else if (parentScope instanceof ConstructorScope) {
            final ConstructorScope constructorScope = (ConstructorScope) parentScope;

            for (final Parameter parameter : constructorScope.parameters) {
                if (parameter.equals(this)) break;
                index += 4;
            }
        } else {
            System.err.println("Param without parent method or constructor.");
            System.exit(42);
        }

        code.add("mov dword eax, [ebp + " + index + "]");

        return code;
    }
}
