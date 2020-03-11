package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

public class ArrayCreationExpression extends Expression {
    final Expression typeExpression;
    final Expression dimensions;

    public ArrayCreationExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        typeExpression = generateExpressionScope(head.getChild(1), this);

        final ASTHead dims = head.getChild(0);

        if (dims.getChildren().size() == 3) {
            dimensions = generateExpressionScope(dims.getChild(1), this);
        } else {
            dimensions = null;
        }
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        typeExpression.linkTypesToQualifiedNames(rootClass);
        dimensions.linkTypesToQualifiedNames(rootClass);
        type = new Type(typeExpression.type, true);
    }

    @Override
    public void checkTypeSoundness() {
        // TODO: Check whether parameters match constructor

        if (dimensions.type.prim_type != Type.PRIM_TYPE.INT) {
            System.err.println("Unsound Type: Array Creation");
            System.exit(42);
        }
    }
}
