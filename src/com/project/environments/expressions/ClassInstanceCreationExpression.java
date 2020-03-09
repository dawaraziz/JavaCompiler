package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

public class ClassInstanceCreationExpression extends Expression {
    final Expression classType;
    final Expression argList;

    public ClassInstanceCreationExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        if (head.getChildren().size() == 5) {
            argList = generateExpressionScope(head.getChild(1), this);
            classType = generateExpressionScope(head.getChild(3), this);
        } else {
            argList = null;
            classType = generateExpressionScope(head.getChild(2), this);
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
