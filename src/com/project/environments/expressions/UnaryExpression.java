package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

import static com.project.environments.expressions.LiteralExpression.LITERAL_TYPE.INTEGER_LITERAL;


public class UnaryExpression extends Expression {

    final Expression RHS;

    public UnaryExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        RHS = generateExpressionScope(head.getChild(0), this);

    }

    @Override
    public boolean isVariableNameUsed(String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {
        RHS.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        if (RHS.type.prim_type != Type.PRIM_TYPE.INT) {
            if (RHS.isLiteralExpression() && ((LiteralExpression) RHS).literal_type != INTEGER_LITERAL) {
                    System.err.println("Unsound type: Unary");
                    System.exit(42);
            }
        }
    }
}
