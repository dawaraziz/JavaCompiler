package com.project.environments.scopes;

import com.project.environments.structure.Name;

import java.util.ArrayList;

public class ImportScope extends Scope {
    public enum IMPORT_TYPE {
        SINGLE,
        ON_DEMAND
    }

    public final IMPORT_TYPE importType;
    public final Name name;

    public ImportScope(final IMPORT_TYPE importType,
                       final ArrayList<String> name,
                       final ClassScope parentScope) {
        this.importType = importType;
        this.name = new Name(name);
        this.parentScope = parentScope;
    }

    public ImportScope(final IMPORT_TYPE importType,
                       final String name,
                       final ClassScope parentScope) {
        this.importType = importType;
        this.name = new Name(name);
        this.parentScope = parentScope;
    }

    public ImportScope(final IMPORT_TYPE importType,
                       final Name name,
                       final ClassScope parentScope) {
        this.importType = importType;
        this.name = name;
        this.parentScope = parentScope;
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

    String getSimpleName() {
        return (importType == IMPORT_TYPE.ON_DEMAND) ? null : name.getSimpleName();
    }

    Name getPackageName() {
        return (importType == IMPORT_TYPE.ON_DEMAND) ? name : name.getPackageName();
    }
}
