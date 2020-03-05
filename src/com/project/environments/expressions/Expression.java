package com.project.environments.expressions;

import com.project.environments.scopes.Scope;
import com.project.environments.ast.ASTHead;

abstract public class Expression extends Scope {
    public static Expression generateExpressionScope
            (final ASTHead expression, final Scope parentScope) {
        return null;
    }
}
