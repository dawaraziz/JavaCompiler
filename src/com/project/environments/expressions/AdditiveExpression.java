package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

public class AdditiveExpression extends Expression {
    final Expression LHS;
    final Expression RHS;

    public AdditiveExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        if (head.getChildren().size() == 2) {
            LHS = null;
            RHS = generateExpressionScope(head.getChild(0), this);
        } else {
            LHS = generateExpressionScope(head.getChild(2), this);
            RHS = generateExpressionScope(head.getChild(0), this);
        }
    }


    @Override
    public boolean isVariableNameUsed(String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {
        if (LHS != null) LHS.linkTypesToQualifiedNames(rootClass);
        RHS.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        if (LHS != null) {
            if ((LHS.type.isString() && RHS.type.prim_type == Type.PRIM_TYPE.INT) ||
                    (LHS.type.prim_type == Type.PRIM_TYPE.INT && RHS.type.isString())) {

            }
            else if (LHS.type != RHS.type) {
                System.err.println("Unsound type: Additive");
                System.exit(42);
            }
        }
    }
}