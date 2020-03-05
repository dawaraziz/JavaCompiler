package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

public class WhileStatement extends Statement {

    final Expression expression;
    final Statement mainBody;

    WhileStatement(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        expression = Expression.generateExpressionScope(head.getChild(2), this);
        mainBody = Statement.generateStatementScope(head.getChild(0), this);
    }

    @Override
    public boolean isVariableNameFree(final String variableName) {
        return parentScope.isVariableNameFree(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        expression.linkTypesToQualifiedNames(rootClass);
        mainBody.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        // TODO: Uncomment when expression types are implemented.
//        if (expression.type.prim_type != BOOLEAN) {
//            System.err.println("Encountered non-boolean while expression.");
//            System.exit(42);
//        }

        expression.checkTypeSoundness();
        mainBody.checkTypeSoundness();
    }
}
