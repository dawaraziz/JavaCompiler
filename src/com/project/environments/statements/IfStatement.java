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

        final boolean hasElse = head.unsafeGetHeadNode()
                .getDirectChildrenWithLexemes("else").size() == 1;

        if (hasElse) {
            expression = generateExpressionScope(head.getChild(4), this);
            ifBody = generateStatementScope(head.getChild(2), this);
            elseBody = generateStatementScope(head.getChild(0), this);
        } else {
            expression = generateExpressionScope(head.getChild(2), this);
            ifBody = generateStatementScope(head.getChild(0), this);
            elseBody = null;
        }
    }

    @Override
    public boolean isVariableNameFree(final String variableName) {
        return parentScope.isVariableNameFree(variableName);
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
