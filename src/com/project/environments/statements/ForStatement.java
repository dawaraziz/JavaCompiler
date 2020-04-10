package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.expressions.LiteralExpression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

import java.util.ArrayList;
import java.util.HashMap;

public class ForStatement extends Statement {
    public Scope forInit;
    private Expression forExpression;
    private Expression forUpdate;
    private final Statement forBody;

    private static long labelCounter = 0;

    @Override
    public void checkReturnedTypes(final Type type, final HashMap<String, ClassScope> classmap) {
        forBody.checkReturnedTypes(type, classmap);
    }

    @Override
    public void assignReachability() {
        final boolean isLiteralTrue;
        final boolean isLiteralFalse;

        if (forExpression instanceof LiteralExpression) {
            isLiteralTrue = ((LiteralExpression) forExpression).isTrue()
                    && !(forBody instanceof EmptyStatement);
            isLiteralFalse = ((LiteralExpression) forExpression).isFalse()
                    && !(forBody instanceof EmptyStatement);
        } else {
            isLiteralTrue = false;
            isLiteralFalse = false;
        }

        out = in && forExpression != null && !isLiteralTrue;

        forBody.in = in && !isLiteralFalse;
        forBody.assignReachability();
    }

    @Override
    public void checkReachability() {
        if (!in) {
            System.err.println("Found unreachable for statement.");
            System.exit(42);
        }

        forBody.checkReachability();
    }

    ForStatement(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;
        this.type = null;

        for (int i = head.getChildren().size() - 1; i >= 3; --i) {
            if (head.getChild(i).getLexeme().equals("(")
                    && head.getChild(i - 2).getLexeme().equals(";")
                    && head.getChild(i - 1).getKind() == null) {
                final ASTHead forInitHead = head.getChild(i - 1);
                if (forInitHead.getLexeme().equals("LOCALVARIABLEDECLARATION")) {
                    forInit = Statement.generateStatementScope(forInitHead, this);
                } else {
                    forInit = Expression.generateExpressionScope(forInitHead, this);
                }
            } else if (head.getChild(i).getLexeme().equals(";")
                    && head.getChild(i - 2).getLexeme().equals(";")
                    && head.getChild(i - 1).getKind() == null) {
                forExpression = Expression.generateExpressionScope(head.getChild(i - 1), this);
            } else if (head.getChild(i).getLexeme().equals(";")
                    && head.getChild(i - 2).getLexeme().equals(")")
                    && head.getChild(i - 1).getKind() == null) {
                forUpdate = Expression.generateExpressionScope(head.getChild(i - 1), this);
            }
        }

        forBody = Statement.generateStatementScope(head.getChild(0), this);
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        if (forInit != null
                && forInit.isVariableNameUsed(variableName)) {
            return true;
        } else {
            return parentScope.isVariableNameUsed(variableName);
        }
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        if (forInit != null) forInit.linkTypesToQualifiedNames(rootClass);
        if (forExpression != null) forExpression.linkTypesToQualifiedNames(rootClass);
        if (forUpdate != null) forUpdate.linkTypesToQualifiedNames(rootClass);
        if (forBody != null) forBody.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        if (forInit != null) forInit.checkTypeSoundness();
        if (forExpression != null) forExpression.checkTypeSoundness();
        if (forUpdate != null) forUpdate.checkTypeSoundness();
        if (forBody != null) forBody.checkTypeSoundness();
    }

    @Override
    public ArrayList<String> generatei386Code() {
        labelCounter += 1;

        final ArrayList<String> code = new ArrayList<>();

        code.add(setLoopLabel());

        // Define the for loop variable.
        if (forInit != null) code.addAll(forInit.generatei386Code());

        // Evaluates the for loop condition.
        if (forExpression != null) {
            code.addAll(forExpression.generatei386Code());
            code.add("cmp eax, 0; Check if expression returns false.");
            code.add("je " + callEndLabel() + "; Jump to end of loop if expr. is false.");
        }

        // Evaluates the for loop body.
        code.addAll(forBody.generatei386Code());

        // Update the for loop variable.
        code.addAll(forUpdate.generatei386Code());

        // Goes back to the top of the loop.
        code.add("jmp " + callLoopLabel() + "; Jump to top of loop.");

        code.add(setEndLabel());

        return code;
    }

    private String setLoopLabel() {
        return "loop_" + labelCounter + ":";
    }

    private String callLoopLabel() {
        return "loop_" + labelCounter;
    }

    private String setEndLabel() {
        return "end_" + labelCounter + ":";
    }

    private String callEndLabel() {
        return "end_" + labelCounter;
    }
}
