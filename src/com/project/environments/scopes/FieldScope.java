package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;

import java.util.ArrayList;

public class FieldScope extends Scope {
    public final ArrayList<String> modifiers;
    public final Expression initializer;

    FieldScope(final ASTHead head, final ClassScope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = ast.getFieldName();

        this.type = ast.getFieldType();
        this.modifiers = head.getFieldModifiers();
        this.initializer = Expression.generateExpressionScope(ast.getFieldInitializer(), this);
    }

    @Override
    public boolean isVariableNameFree(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        type.linkType(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        // TODO: Uncomment when expression types are implemented.
//        if (!type.equals(initializer.type)) {
//            System.err.println("Field initializer has wrong type.");
//            System.exit(42);
//        }
    }
}
