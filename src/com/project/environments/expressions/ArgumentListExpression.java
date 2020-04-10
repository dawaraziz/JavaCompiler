package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.project.scanner.structure.Kind.COMMA;

public class ArgumentListExpression extends Expression {
    public ArrayList<Expression> arguments;

    ArgumentListExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        arguments = new ArrayList<>();
        for (final ASTHead child : head.getChildren()) {
            if (child.getKind() != COMMA)
                arguments.add(generateExpressionScope(child, this));
        }
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        arguments.forEach(c -> c.linkTypesToQualifiedNames(rootClass));
    }

    @Override
    public void checkTypeSoundness() {

    }

    @Override
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();

        for (int i = arguments.size() - 1; i >= 0; --i) {
            code.addAll(arguments.get(i).generatei386Code());

            // We expect to call a method directly after this, so for this expression
            // and this expression only, we push without popping.
            code.add("push eax");
        }

        return code;
    }
}
