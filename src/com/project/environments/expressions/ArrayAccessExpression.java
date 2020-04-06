package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

import static com.project.environments.structure.Type.PRIM_TYPE.*;

public class ArrayAccessExpression extends Expression {
    final Expression LHS;
    final Expression RHS;

    public ArrayAccessExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        RHS = generateExpressionScope(head.getChild(1), this);
        LHS = generateExpressionScope(head.getChild(3), this);

        if (LHS instanceof NameExpression
                && !((NameExpression) LHS).isExpressionName()) {
            System.err.println("Found array access with non-expression name LSH");
            System.exit(42);
        }

    }


    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return true;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        LHS.linkTypesToQualifiedNames(rootClass);
        RHS.linkTypesToQualifiedNames(rootClass);
        this.type = new Type(LHS.type, false);
    }

    @Override
    public void checkTypeSoundness() {
        if (!RHS.type.isNumericType()) {
            System.err.println("Found array access with non-integer RHS.");
            System.exit(42);
        }
    }

    @Override
    public String code(){
        StringBuilder assembly = new StringBuilder();

        assembly.append(LHS.code());
        assembly.append("push eax");
        assembly.append(RHS.code());
        assembly.append("pop ebx");
        assembly.append("shl eax,2");
        assembly.append("add eax,8"); // Are we storing class and length of array at the front?
        assembly.append("add eax,ebx");

        return assembly.toString();
    }
}
