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

        unary = generateExpressionScope(head.getChild(0), this);
        if (head.getChildren().size() == 6) cast = generateExpressionScope(head.getChild(3), this);
        else cast = generateExpressionScope(head.getChild(2), this);
    }

    @Override
    public boolean isVariableNameFree(String variableName) {
        return parentScope.isVariableNameFree(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {
        if (cast != null) cast.linkTypesToQualifiedNames(rootClass);
        if (unary != null) unary.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
    }
}
