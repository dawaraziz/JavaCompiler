package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

import java.util.ArrayList;

public class EmptyStatement extends Statement {
    @Override
    public void assignReachability() {
        out = in;
    }

    @Override
    public void checkReachability() {
        if (!in) {
            System.err.println("Found unreachable empty statement.");
            System.exit(42);
        }
    }

    EmptyStatement(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;
        this.type = null;
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return parentScope.isVariableNameUsed(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {

    }

    @Override
    public void checkTypeSoundness() {

    }

    @Override
    public ArrayList<String> generatei386Code() {
        return new ArrayList<>();
    }
}
