package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

import java.util.ArrayList;

import static com.project.environments.scopes.MethodScope.generateEpilogueCode;
import static com.project.environments.scopes.MethodScope.generatePrologueCode;

public class ArrayCreationExpression extends Expression {
    private final Expression arrayTypeExpr;
    private final Expression dimensions;

    ArrayCreationExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        arrayTypeExpr = generateExpressionScope(head.getChild(1), this);

        final ASTHead dims = head.getChild(0);

        if (dims.getChildren().size() == 3) {
            dimensions = generateExpressionScope(dims.getChild(1), this);
        } else {
            dimensions = null;
        }
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        arrayTypeExpr.linkTypesToQualifiedNames(rootClass);
        dimensions.linkTypesToQualifiedNames(rootClass);
        type = new Type(arrayTypeExpr.type, true);
    }

    @Override
    public void checkTypeSoundness() {
        // TODO: Check whether parameters match constructor

        if (dimensions.type.prim_type != Type.PRIM_TYPE.INT) {
            System.err.println("Unsound Type: Array Creation");
            System.exit(42);
        }
    }

    @Override
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();

        // Find how big we want our array to be.
        code.addAll(dimensions.generatei386Code());

        // Saves the array length for later
        code.add("push eax");

        // Add a spot for the vtable and length.
        code.add("add eax, 8");

        // Generates the actual memory.
        code.addAll(generatePrologueCode());
        code.add("call __malloc");
        code.addAll(generateEpilogueCode());

        // Puts the array length in the correct spot.
        code.add("pop [eax + 4]");

        // Puts the vtable of the array object in the correct spot.
        if (arrayTypeExpr instanceof TypeExpression) {
            code.add("mov dword [eax + 4], 0");
        } else if (arrayTypeExpr instanceof NameExpression) {
            final NameExpression nameExpr = (NameExpression) arrayTypeExpr;

            if (!(nameExpr.namePointer instanceof ClassScope)) {
                System.err.println("Non class array name expr. creation.");
                System.exit(42);
            }

            final ClassScope classExpr = (ClassScope) nameExpr.namePointer;

            code.add("mov dword [eax + 4], " + classExpr.callVtableLabel());
        } else {
            System.err.println("Non class or prim array creation.");
            System.exit(42);
        }

        return code;
    }
}
