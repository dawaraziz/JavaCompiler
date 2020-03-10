package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.Scope;

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
            } else if (expression.isUnaryNotPlusMinusExpr()) {
                childScopes.add(new UnaryNotPlusMinusExpr(expression, parentScope));
            } else if (expression.isUnaryExpr()) {
                childScopes.add(new UnaryExpression(expression, parentScope));
            } else if (expression.isLiteralExpr()) {
                childScopes.add(new LiteralExpression(expression, parentScope));
            } else if (expression.isTypeExpr()) {
                childScopes.add(new TypeExpression(expression, parentScope));
            } else if (expression.isNameExpr()) {
                childScopes.add(new NameExpression(expression, parentScope));
            } else if (expression.isMethodInvocationExpr()) {
                childScopes.add(new MethodInvocationExpression(expression, parentScope));
            } else if (expression.isArrayCreationExpr()) {
                childScopes.add(new ArrayCreationExpression(expression, parentScope));
            } else if (expression.isPrimaryNoNewArrayExpr()) {
                childScopes.add(new PrimaryNoNewArrayExpression(expression, parentScope));
            } else if (expression.isClassInstanceCreationExpr()) {
                childScopes.add(new ClassInstanceCreationExpression(expression, parentScope));
            } else if (expression.isArrayTypeExpr()) {
                childScopes.add(new ArrayTypeExpression(expression, parentScope));
            } else if (expression.isArgumentListExpr()) {
                childScopes.add(new ArgumentListExpression(expression, parentScope));
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

    public boolean isLiteralExpression() {
        return this instanceof LiteralExpression;
    }
}
