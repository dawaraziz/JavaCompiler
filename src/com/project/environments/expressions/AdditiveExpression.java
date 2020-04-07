package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

import static com.project.environments.structure.Type.PRIM_TYPE.INT;

public class AdditiveExpression extends Expression {
    final Expression LHS;
    final Expression RHS;

    public AdditiveExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        if (head.getChildren().size() > 3) {
            LHS = generateExpressionScope(head.getChild(head.getChildren().size() - 1), this);
            RHS = generateExpressionScope(head.generateBaseSubHead(), this);
        } else if (head.getChildren().size() == 2) {
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
        LHS.linkTypesToQualifiedNames(rootClass);
        RHS.linkTypesToQualifiedNames(rootClass);

        if (LHS.type.isNumericType() && RHS.type.isNumericType()) {
            this.type = new Type(INT);
        } else if (LHS.type.isString() || RHS.type.isString()) {
            this.type = Type.generateStringType();
        } else {
            System.err.println("Could not determine AdditiveExpression type.");
            System.exit(42);
        }
    }

    @Override
    public void checkTypeSoundness() {
        RHS.checkTypeSoundness();
        LHS.checkTypeSoundness();

        if (LHS.type.isString() || RHS.type.isString()) return;

        if (!LHS.type.isNumericType() || !RHS.type.isNumericType()) {
            System.err.println("Numeric additive expression is invalid.");
            System.exit(42);
        }
    }


    public String code() {
        StringBuilder assembly = new StringBuilder();

        assembly.append(LHS.code());
        assembly.append("\n");
        assembly.append("push eax;");
        assembly.append("\n");
        assembly.append(RHS.code());
        assembly.append("\n");
        assembly.append("pop ebx;");
        assembly.append("\n");
        assembly.append("add ebx, eax;");
        assembly.append("\n");
        assembly.append("mov eax, ebx;");
        assembly.append("\n");


        return assembly.toString();
    }


}
