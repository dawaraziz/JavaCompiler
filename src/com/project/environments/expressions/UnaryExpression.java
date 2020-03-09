package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

public class UnaryExpression extends Expression {

    final Expression RHS;

    public UnaryExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        RHS = generateExpressionScope(head.getChild(0), this);

    }

    @Override
    public boolean isVariableNameFree(String variableName) {
        return parentScope.isVariableNameFree(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {
        RHS.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {

    }
}
