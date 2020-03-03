package com.project.environments.structure;

import com.project.environments.ClassScope;

public class Parameter {
    public final Type type;
    public final Name name;

    public Parameter(final Type type, final Name name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
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
}
