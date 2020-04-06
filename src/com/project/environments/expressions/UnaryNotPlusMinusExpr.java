package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

public class UnaryNotPlusMinusExpr extends Expression {
    final Expression nextExpr;

    UnaryNotPlusMinusExpr(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        this.nextExpr = generateExpressionScope(head.getChild(0), this);
    }

    @Override
    public boolean isVariableNameUsed(String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {
        this.nextExpr.linkTypesToQualifiedNames(rootClass);
        this.type = this.nextExpr.type;
    }

    @Override
    public void checkTypeSoundness() {
        this.nextExpr.checkTypeSoundness();
        if ((ast.getChildren().size() == 2) && this.nextExpr.type.prim_type != Type.PRIM_TYPE.BOOLEAN) {
            System.err.println("Unsound type: UnaryNotPlusMinus");
            System.exit(42);
        }
    }

    @Override
    public String code() {
        StringBuilder assembly = new StringBuilder();

        return assembly.append(nextExpr.code()).toString();
    }
}
