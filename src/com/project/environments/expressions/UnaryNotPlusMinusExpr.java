package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

import java.util.ArrayList;

public class UnaryNotPlusMinusExpr extends Expression {
    final Expression nextExpr;

    private static long labelCounter = 0;

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
    public ArrayList<String> generatei386Code() {
        labelCounter += 1;

        final long count = labelCounter;

        final ArrayList<String> code = new ArrayList<>();

        code.addAll(nextExpr.generatei386Code());

        code.add("cmp eax, 0");
        code.add("je " + callNotLabel(count));
        code.add("mov eax, 0; If eax is 1+, set it to 0.");
        code.add("jmp " + callEndLabel(count));
        code.add(setNotLabel(count));
        code.add("mov eax, 1; If eax is 0, set it to 1.");
        code.add(setEndLabel(count));

        return code;
    }

    private String setNotLabel(final long count) {
        return "unary_not_plus_not_" + count + ":";
    }

    private String callNotLabel(final long count) {
        return "unary_not_plus_not_" + count;
    }

    private String setEndLabel(final long count) {
        return "unary_not_plus_end_" + count + ":";
    }

    private String callEndLabel(final long count) {
        return "unary_not_plus_end_" + count;
    }
}
