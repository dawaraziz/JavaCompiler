package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

import static com.project.environments.expressions.Expression.generateExpressionScope;

public class ExpressionStatement extends Statement {
    final Expression expression;

    ExpressionStatement(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        expression = generateExpressionScope(head, this);

        if (expression != null) this.type = expression.type;
    }

    @Override
    public boolean isVariableNameFree(final String variableName) {
        return parentScope.isVariableNameFree(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        expression.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        expression.checkTypeSoundness();
    }
}
