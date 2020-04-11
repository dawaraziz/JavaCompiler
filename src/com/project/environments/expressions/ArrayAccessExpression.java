package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

import java.util.ArrayList;
import java.util.Collection;

import static com.project.environments.scopes.MethodScope.generateEpilogueCode;
import static com.project.environments.scopes.MethodScope.generatePrologueCode;
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
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();

        // Evaluate the RHS to get the index in the array.
        code.addAll(RHS.generatei386Code());

        // Multiply the index by 4, to get the byte offset.
        code.add("mov ebx, 4");
        code.add("mul ebx");

        // Add 8 to skip the length and vtable.
        code.add("add eax, 8");

        // Store the index
        code.add("push eax");

        // Evaluate the LHS to get the memory of the array.
        code.addAll(LHS.generatei386Code());

        // Get the index back;
        code.add("pop ebx");

        // eax should now be the array memory + (index * 4) + 8.
        code.add("add eax, ebx");

        // Get the value at that location.
        code.add("mov eax, [eax]");

        return code;
    }

    public ArrayList<String> generateArrayAddrCode() {
        final ArrayList<String> code = new ArrayList<>();

        // Evaluate the RHS to get the index in the array.
        code.addAll(RHS.generatei386Code());

        // Multiply the index by 4, to get the byte offset.
        code.add("mov ebx, 4");
        code.add("mul ebx");

        // Add 8 to skip the length and vtable.
        code.add("add eax, 8");

        // Store the index
        code.add("push eax");

        // Evaluate the LHS to get the memory of the array.
        code.addAll(LHS.generatei386Code());

        // Get the index back;
        code.add("pop ebx");

        // eax should now be the array memory + (index * 4) + 8.
        code.add("add eax, ebx");

        return code;
    }
}
