package com.project.environments;

import com.project.environments.structure.Name;

import java.util.ArrayList;

public class ImportScope extends Scope {
    public enum IMPORT_TYPE {
        SINGLE,
        ON_DEMAND
    }

    public final IMPORT_TYPE type;
    public final Name name;

    public ImportScope(final IMPORT_TYPE type, final ArrayList<String> name, final ClassScope parentScope) {
        this.type = type;
        this.name = new Name(name);
        this.parentScope = parentScope;
    }

    public ImportScope(final IMPORT_TYPE type, final String name, final ClassScope parentScope) {
        this.type = type;
        this.name = new Name(name);
        this.parentScope = parentScope;
    }

    public ImportScope(final IMPORT_TYPE type, final Name name, final ClassScope parentScope) {
        this.type = type;
        this.name = name;
        this.parentScope = parentScope;
    }

    @Override
    boolean isInitCheck(final String variableName) {
        return false;
    }

    public Name generateFullName(final Name name) {
        if (type == IMPORT_TYPE.ON_DEMAND) return null;

        if (this.name.containsSuffixName(name)) {
            return this.name;
        } else {
            return null;
        }
    }

    public String getSimpleName() {
        if (type == IMPORT_TYPE.ON_DEMAND) return null;
        return name.getSimpleName();
    }

    public Name getPackageName() {
        if (type == IMPORT_TYPE.ON_DEMAND) {
            return name;
        } else {
            return name.getPackageName();
        }
    }
}
