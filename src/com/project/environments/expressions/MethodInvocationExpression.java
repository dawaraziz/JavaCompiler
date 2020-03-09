package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

public class MethodInvocationExpression extends Expression {
    final Expression methodName;
    final Expression parameters;

    MethodInvocationExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        if (ast.getChildren().size() == 4) {
            methodName = generateExpressionScope(head.getChild(3), this);
            parameters = generateExpressionScope(head.getChild(1), this);
        } else {
            methodName = generateExpressionScope(head.getChild(2), this);
            parameters = null;
        }
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        // TODO:
    }

    @Override
    public void checkTypeSoundness() {
        // TODO:
    }
}
