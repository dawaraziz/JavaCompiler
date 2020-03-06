package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Name;

import static com.project.environments.ast.ASTNode.lexemesToStringList;

public class NameExpression extends Expression {
    final Name nameClass;

    public NameExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        nameClass = new Name(lexemesToStringList(head.unsafeGetHeadNode().getLeafNodes()));
    }

    @Override
    public boolean isVariableNameFree(final String variableName) {
        return parentScope.isVariableNameFree(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        // TODO:
    }

    @Override
    public void checkTypeSoundness() {
    }

    boolean isExpressionName() {
        return ast.isExpressionName();
    }
}
