package com.project.environments.expressions;

import com.project.environments.scopes.Scope;
import com.project.environments.ast.ASTHead;
import com.project.environments.statements.*;

import java.util.ArrayList;

abstract public class Expression extends Scope {

    public static ArrayList<Expression> generateExpressionScope
            (final ArrayList<ASTHead> expressions, final Scope parentScope) {

        final ArrayList<Expression> childScopes = new ArrayList<>();

        if (expressions == null) return childScopes;

        for (int i = expressions.size() - 1; i >= 0; --i) {
            final ASTHead expression = expressions.get(i);

            if (expression.isArrayAccessExpression()) {
                childScopes.add(new ArrayAccessExpression(expression, parentScope));
            } else if (expression.isCastExpr()) {
                childScopes.add(new CastExpression(expression, parentScope));
            } else if (expression.isAdditiveExpr()) {
                childScopes.add(new AdditiveExpression(expression, parentScope));
            } else {
                childScopes.add(new BaseExpression(expression, parentScope));
            }
        }

        return childScopes;
    }

    public static Expression generateExpressionScope
            (final ASTHead expression, final Scope parentScope) {

        if (expression == null) return null;

        final ArrayList<ASTHead> nodes = new ArrayList<>();
        nodes.add(expression);
        return generateExpressionScope(nodes, parentScope).get(0);
    }
}
