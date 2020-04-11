package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

import java.util.ArrayList;

public class ArrayTypeExpression extends Expression {
    private final Expression typeName;

    public ArrayTypeExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        typeName = generateExpressionScope(head.getChild(2), this);
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        typeName.linkTypesToQualifiedNames(rootClass);
        type = new Type(typeName.type, true);
    }

    @Override
    public void checkTypeSoundness() {
    }

    @Override
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();
        code.addAll(typeName.generatei386Code());
        return code;
    }
}
