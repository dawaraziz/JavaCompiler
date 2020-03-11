package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

public class CastExpression extends Expression{
    final Expression cast;
    final Expression unary;
    final boolean isArrayCast;

    public CastExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        if (head.getChildren().size() == 4) {
            cast = generateExpressionScope(head.getChild(2), this);
            unary = generateExpressionScope(head.getChild(0), this);
            isArrayCast = false;
        } else if (head.getChildren().size() == 5) {
            cast = generateExpressionScope(head.getChild(3), this);
            unary = generateExpressionScope(head.getChild(0), this);
            isArrayCast = true;
        } else {
            cast = generateExpressionScope(head.getChild(head.getChildren().size() - 2), this);
            isArrayCast = head.getChild(head.getChildren().size() - 3).getLexeme().equals("DIMS");
            unary = generateExpressionScope(head.generateCastSubHead(isArrayCast), this);
        }
    }

    @Override
    public boolean isVariableNameUsed(String variableName) {
        return true;
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {
        cast.linkTypesToQualifiedNames(rootClass);
        if (unary != null) unary.linkTypesToQualifiedNames(rootClass);

        type = cast.type;
    }

    @Override
    public void checkTypeSoundness() {
        cast.checkTypeSoundness();
        unary.checkTypeSoundness();
    }
}
