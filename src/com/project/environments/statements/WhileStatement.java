package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.expressions.LiteralExpression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;
import java.util.HashMap;

public class WhileStatement extends Statement {

    private final Expression expression;
    private final Statement mainBody;

    private static long labelCounter = 0;

    @Override
    public void checkConditionals() {
        // expression must evaluate to boolean
        System.out.println("Expression type is: " + expression);
        if (!(expression.evaluatesTo() == Kind.BOOLEAN)) {
            System.err.println("While Statement does not evaluate to a boolean");
            System.exit(42);
        }
    }

    @Override
    public void checkReturnedTypes(final Type type, final HashMap<String, ClassScope> classmap) {
        mainBody.checkReturnedTypes(type, classmap);
    }

    @Override
    public void assignReachability() {
        final boolean isLiteralTrue;
        final boolean isLiteralFalse;

        if (expression instanceof LiteralExpression) {
            isLiteralTrue = ((LiteralExpression) expression).isTrue()
                    && !(mainBody instanceof EmptyStatement);
            isLiteralFalse = ((LiteralExpression) expression).isFalse();
        } else {
            isLiteralTrue = false;
            isLiteralFalse = false;
        }

        out = in && !isLiteralTrue;

        mainBody.in = in && !isLiteralFalse;
        mainBody.assignReachability();
    }

    @Override
    public void checkReachability() {
        if (!in) {
            System.err.println("Found unreachable while statement.");
            System.exit(42);
        }

        mainBody.checkReachability();
    }

    WhileStatement(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        expression = Expression.generateExpressionScope(head.getChild(2), this);
        mainBody = Statement.generateStatementScope(head.getChild(0), this);
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return parentScope.isVariableNameUsed(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        expression.linkTypesToQualifiedNames(rootClass);
        mainBody.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        // TODO: Uncomment when expression types are implemented.
//        if (expression.type.prim_type != BOOLEAN) {
//            System.err.println("Encountered non-boolean while expression.");
//            System.exit(42);
//        }

        expression.checkTypeSoundness();
        mainBody.checkTypeSoundness();
    }

    private String setLoopLabel(final long count) {
        return "while_loop_" + count + ":";
    }

    private String callLoopLabel(final long count) {
        return "while_loop_" + count;
    }

    private String setEndLabel(final long count) {
        return "while_end_" + count + ":";
    }

    private String callEndLabel(final long count) {
        return "while_end_" + count;
    }

    @Override
    public ArrayList<String> generatei386Code() {
        labelCounter += 1;

        final long count = labelCounter;

        final ArrayList<String> code = new ArrayList<>();

        code.add(setLoopLabel(count));

        // Evaluate the loop condition.
        code.addAll(expression.generatei386Code());
        code.add("cmp eax, 0; Check if expression returns false.");
        code.add("je " + callEndLabel(count) + "; Jump to end of loop if expr. is false.");

        // Evaluate the loop body.
        code.addAll(mainBody.generatei386Code());
        code.add("jmp " + callLoopLabel(count) + "; Jump to top of loop.");

        code.add(setEndLabel(count));

        return code;
    }
}
