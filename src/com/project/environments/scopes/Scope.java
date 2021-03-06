package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.MethodInvocationExpression;
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

    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();
//        code.add("Placeholder " + this.getClass().getName());
        return code;
    }

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

    protected ConstructorScope getParentConstructor() {
        if (parentScope == null) return null;
        if (this instanceof ConstructorScope) {
            return (ConstructorScope) this;
        } else {
            return parentScope.getParentConstructor();
        }
    }

    protected ArrayList<DefinitionStatement> getParentLocalDefinitions() {
        final ArrayList<DefinitionStatement> ret = new ArrayList<>();

        if (this instanceof DefinitionStatement) {
            ret.add((DefinitionStatement) this);
        }

        if (parentScope != null) ret.addAll(parentScope.getParentLocalDefinitions());

        return ret;
    }

    public ClassScope getParentClass() {
        if (this instanceof ClassScope) {
            return (ClassScope) this;
        } else {
            return parentScope.getParentClass();
        }
    }

    protected DefinitionStatement getDefinitionScope(final String identifier) {
        if (this instanceof DefinitionStatement) {
            if (this.name.equals(identifier)) {
                return (DefinitionStatement) this;
            }
        } else if (this instanceof ForStatement) {
            if (((ForStatement) this).forInit instanceof DefinitionStatement) {
                if (((ForStatement) this).forInit.name.equals(identifier)) {
                    return (DefinitionStatement) ((ForStatement) this).forInit;
                }
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

    protected MethodInvocationExpression getMethodInvocation() {
        if (this instanceof MethodInvocationExpression) {
            return (MethodInvocationExpression) this;
        } else {
            return parentScope.getMethodInvocation();
        }
    }
}
