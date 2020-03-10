package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.statements.DefinitionStatement;
import com.project.environments.statements.ForStatement;
import com.project.environments.structure.Type;

import java.util.ArrayList;

public abstract class Scope {
    public String name;
    public ASTHead ast;
    public Scope parentScope;
    public Type type;

    public abstract boolean isVariableNameUsed(final String variableName);

    public abstract void linkTypesToQualifiedNames(final ClassScope rootClass);

    public abstract void checkTypeSoundness();

    protected ArrayList<FieldScope> getParentFields() {
        final ArrayList<FieldScope> ret = new ArrayList<>();

        if (this instanceof FieldScope) {
            ret.add((FieldScope) this);
        }

        if (this instanceof ClassScope) {
            ((ClassScope) this).fieldTable.forEach(c -> ret.addAll(c.getParentFields()));
        }

        ret.addAll(parentScope.getParentFields());

        return ret;
    }

    protected ArrayList<MethodScope> getParentMethodList() {
        final ArrayList<MethodScope> ret = new ArrayList<>();

        if (this instanceof MethodScope) {
            ret.add((MethodScope) this);
        }

        if (this instanceof ClassScope) {
            ((ClassScope) this).fieldTable.forEach(c -> ret.addAll(c.getParentMethodList()));
        }

        ret.addAll(parentScope.getParentMethodList());

        return ret;
    }

    protected MethodScope getParentMethod() {
        if (parentScope == null) return null;
        if (this instanceof MethodScope) {
            return (MethodScope) this;
        } else {
            return parentScope.getParentMethod();
        }
    }

    protected ArrayList<DefinitionStatement> getParentLocalDefinitions() {
        final ArrayList<DefinitionStatement> ret = new ArrayList<>();

        if (this instanceof DefinitionStatement) {
            ret.add((DefinitionStatement) this);
        }

        ret.addAll(parentScope.getParentLocalDefinitions());

        return ret;
    }

    protected ClassScope getParentClass() {
        if (this instanceof ClassScope) {
            return (ClassScope) this;
        } else {
            return parentScope.getParentClass();
        }
    }

    protected DefinitionStatement getDefinitionScope(String identifier) {
        if (this instanceof DefinitionStatement) {
            if (this.name.equals(identifier)) {
                return (DefinitionStatement) this;
            }
        }

        if (parentScope instanceof ForStatement) {
            if ((((ForStatement) parentScope).forInit != null) && ((ForStatement) parentScope).forInit.name != null) {
                if (((ForStatement) parentScope).forInit.name.equals(identifier)) {
                    return (DefinitionStatement) ((ForStatement) parentScope).forInit;
                }
            }
        }

        if (parentScope == null) return null;

        return parentScope.getDefinitionScope(identifier);
    }
}
