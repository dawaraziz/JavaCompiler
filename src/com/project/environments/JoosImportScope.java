package com.project.environments;

import com.project.environments.structure.Name;

import java.util.ArrayList;

public class JoosImportScope extends Scope {
    public enum IMPORT_TYPE {
        SINGLE,
        ON_DEMAND
    }

    public final IMPORT_TYPE type;
    public final Name name;

    public JoosImportScope(final IMPORT_TYPE type, final ArrayList<String> name, final JoosClassScope parentScope) {
        this.type = type;
        this.name = new Name(name);
        this.parentScope = parentScope;
    }

    public JoosImportScope(final IMPORT_TYPE type, final String name, final JoosClassScope parentScope) {
        this.type = type;
        this.name = new Name(name);
        this.parentScope = parentScope;
    }
}
