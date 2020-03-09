package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

import static com.project.environments.expressions.Expression.generateExpressionScope;

public class IfStatement extends Statement {
    public final Expression expression;
    public final Statement ifBody;
    public final Statement elseBody;

    IfStatement(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        final boolean hasElse = head.getChildren().size() > 5;

        expression = generateExpressionScope(head.getChild(head.getChildren().size() - 3),
                this);
        ifBody = generateStatementScope(head.getChild(head.getChildren().size() - 5),
                this);

        if (hasElse) {
            if (head.getChildren().size() == 7) {
                elseBody = generateStatementScope(head.getChild(0), this);
            } else {
                elseBody = generateStatementScope(head.generateIfSubHead(), this);
            }
        } else {
            elseBody = null;
        }
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return parentScope.isVariableNameUsed(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        expression.linkTypesToQualifiedNames(rootClass);
        ifBody.linkTypesToQualifiedNames(rootClass);
        if (elseBody != null) elseBody.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        // TODO: Uncomment when expression types are implemented.
//        if (expression.type.prim_type != BOOLEAN) {
//            System.err.println("Encountered non-boolean if expression.");
//            System.exit(42);
//        }

        expression.checkTypeSoundness();
        ifBody.checkTypeSoundness();
        if (elseBody != null) elseBody.checkTypeSoundness();
    }
}
