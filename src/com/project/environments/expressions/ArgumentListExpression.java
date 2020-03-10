package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.project.scanner.structure.Kind.COMMA;

public class ArgumentListExpression extends Expression {
    ArrayList<Expression> arguments;

    ArgumentListExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        arguments = new ArrayList<>();
        for (final ASTHead child : head.getChildren()) {
            if (child.getKind() != COMMA)
                arguments.add(generateExpressionScope(child, this));
        }
        Collections.reverse(arguments);
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {

    }

    @Override
    public void checkTypeSoundness() {

    }
}
