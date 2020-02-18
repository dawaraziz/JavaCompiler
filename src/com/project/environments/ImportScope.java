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

    @Override
    boolean isInitCheck(final String variableName) {
        return false;
    }
}
