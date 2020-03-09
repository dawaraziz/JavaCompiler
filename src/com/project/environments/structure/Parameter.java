package com.project.environments.structure;

import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

public class Parameter extends Scope {
    public Parameter(final Type type, final Name name) {
        this.type = type;
        this.name = name.getSimpleName();
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
    public void checkTypeSoundness() {

    }
}
