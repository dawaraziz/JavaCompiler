package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.scanner.structure.Kind;

public class BaseExpression extends Expression {
    final Expression LHS;
    final Expression singular;
    final Expression RHS;

    public BaseExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        if (head.getChildren().size() == 3
                && head.getChild(0).getKind() == Kind.PAREN_CLOSE
                && head.getChild(2).getKind() == Kind.PAREN_OPEN) {
            LHS = null;
            singular = generateExpressionScope(head.getChild(1), this);
            RHS = null;
        } else if (head.getChildren().size() == 2) {
            LHS = null;
            singular = generateExpressionScope(head.getChild(0), this);
            RHS = null;
        } else {
            LHS = generateExpressionScope(head.getChild(2), this);
            singular = null;
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
        if (LHS != null && LHS.type != RHS.type) {
            System.err.println("Unsound type: Base Expression");
            System.exit(42);
        }
    }
}