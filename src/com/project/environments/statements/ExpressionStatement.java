package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

import static com.project.environments.expressions.Expression.generateExpressionScope;

public class ExpressionStatement extends Statement {
    final Expression expression;

    @Override
    public void assignReachability() {
        out = in;
    }

    @Override
    public void checkReachability() {
        if (!in) {
            System.err.println("Found unreachable expression statement.");
            System.exit(42);
        }
    }

    ExpressionStatement(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        expression = generateExpressionScope(head.getChild(1), this);

        if (expression != null) this.type = expression.type;
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return parentScope.isVariableNameUsed(variableName);
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
