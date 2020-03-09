package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

public class PrimaryNoNewArrayExpression extends Expression {
    final Expression nextExpr;

    public PrimaryNoNewArrayExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        if (head.getChildren().size() == 1 && !head.getChild(0).getLexeme().equals("this")) {
            nextExpr = generateExpressionScope(head.getChild(0), this);
        } else if (head.getChildren().size() == 3) {
            nextExpr = generateExpressionScope(head.getChild(1), this);
        } else {
            nextExpr = new TypeExpression(head.getChild(0), this);
        }
    }

    @Override
    public boolean isVariableNameUsed(String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {

    }

    @Override
    public void checkTypeSoundness() {

    }
}
