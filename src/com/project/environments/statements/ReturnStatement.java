package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.MethodScope;
import com.project.environments.scopes.Scope;

import static com.project.environments.expressions.Expression.generateExpressionScope;
import static com.project.environments.structure.Type.PRIM_TYPE.VOID;

public class ReturnStatement extends Statement {
    final Expression expression;

    public ReturnStatement(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        expression = (head.getChildren().size() == 2)
                ? generateExpressionScope(head.getChild(1), this)
                : null;
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return parentScope.isVariableNameUsed(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        if (expression != null) expression.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        Scope parentMethod = parentScope;
        while (!(parentMethod instanceof MethodScope)) {
            parentMethod = parentMethod.parentScope;
        }

        // TODO: Uncomment when expression types are implemented.
//        if (parentMethod.type.prim_type == VOID && expression != null) {
//            System.err.println("Encountered non-void return in void method.");
//            System.exit(42);
//        } else if (expression == null || !expression.type.equals(parentMethod.type)) {
//            System.err.println("Encountered return with incorrect type.");
//            System.exit(42);
//        }
    }
}
