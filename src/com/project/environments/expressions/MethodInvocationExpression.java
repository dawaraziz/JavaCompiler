package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

import java.util.ArrayList;

public class MethodInvocationExpression extends Expression {
    final Expression methodName;
    final Expression parameters;

    MethodInvocationExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        methodName = generateExpressionScope(head.getChild(head.getChildren().size() - 1), this);

        if (ast.getChildren().size() > 4) {
            parameters = generateExpressionScope(head.generateMethodSubHead(), this);
        } else if (ast.getChildren().size() == 4) {
            parameters = generateExpressionScope(head.getChild(1), this);
        } else {
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
