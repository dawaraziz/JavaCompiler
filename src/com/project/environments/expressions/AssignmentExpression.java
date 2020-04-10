package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;

public class AssignmentExpression extends Expression {
    private final Expression LHS;
    private final Expression RHS;

    AssignmentExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        LHS = generateExpressionScope(head.getChild(2), this);
        RHS = generateExpressionScope(head.getChild(0), this);
    }

    @Override
    public boolean isVariableNameUsed(String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {
        LHS.linkTypesToQualifiedNames(rootClass);
        RHS.linkTypesToQualifiedNames(rootClass);

        type = RHS.type;

        if (type == null) {
            System.err.println("Could not type AssignmentExpression!");
            System.exit(42);
        }
    }

    @Override
    public void checkTypeSoundness() {
        LHS.checkTypeSoundness();
        RHS.checkTypeSoundness();

        if (LHS.type.equals(RHS.type)) return;

        if (LHS.type.isNumericType() && LHS.type.isSmallerNumericType(RHS.type)) return;

        if (LHS.type.equals(Type.generateObjectType()) && RHS.type.isArray) return;

        if (LHS.type.isReferenceType() && RHS.type.isNullType()) return;

        if (LHS.type.isReferenceType() && RHS.type.isReferenceType()) return;

        if (LHS.type.name == null || RHS.type.name == null) {
            System.err.println("Found classes without names.");
            System.exit(42);
        }

        final ClassScope parentClass = this.getParentClass();
        final ClassScope LHSClass = parentClass.classMap.get(LHS.type.name.getDefaultlessQualifiedName());
        final ClassScope RHSClass = parentClass.classMap.get(RHS.type.name.getDefaultlessQualifiedName());

        if (!RHSClass.isSubClassOf(LHSClass)) {
            System.err.println("Could not identify assigned class as subclass.");
            System.exit(42);
        }

        System.err.println("Could not identify assignment type.");
        System.exit(42);
    }

    public Kind evaluatesTo(){
        return booleanOrKind(Kind.NULL);
    }

    @Override
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();

        // Evaluates the LHS. It should always be a NameExpression.
        if (LHS instanceof  NameExpression) {
            code.addAll(((NameExpression) LHS).generateNameAddrCode());
        } else if (LHS instanceof ArrayAccessExpression) {
            // TODO:
        } else if (LHS instanceof FieldAccessExpression) {
            // TODO:
        } else {
            System.err.println("Found non-name or array access LHS of assignment");
            System.exit(42);
        }


        // Since it's a NameExpression, eax should contain a memory location.
        // We save that to the stack for now.
        code.add("push eax");

        // Evaluate the RHS.
        code.addAll(RHS.generatei386Code());

        // Grab the saved LHS memory address.
        code.add("pop ebx");

        // Replace the value at the memory address with the RHS value.
        code.add("mov [ebx], eax");

        return code;
    }
}
