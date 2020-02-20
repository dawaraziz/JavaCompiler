package com.project.environments.structure;

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
        }

        if (obj == this) {
            return true;
        }

        Parameter other = (Parameter) obj;

        if (this.type.equals(other.type)) return true;
        else return false;
    }
}
