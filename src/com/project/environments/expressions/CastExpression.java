package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

public class CastExpression extends Expression{
    final Expression cast;
    final Expression unary;

    public CastExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        unary = generateExpressionScope(head.getChild(head.getChildren().size()-1), this);
        cast = generateExpressionScope(head.getChild(2), this);
    }

    @Override
    public boolean isVariableNameFree(String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {

    }

    @Override
    public void checkTypeSoundness() {

    }
}
