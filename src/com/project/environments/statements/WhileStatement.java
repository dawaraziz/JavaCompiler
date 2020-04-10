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

    final Expression expression;
    final Statement mainBody;

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
    public void checkReturnedTypes(Type type, HashMap<String, ClassScope> classmap) {
        mainBody.checkReturnedTypes(type, classmap);
        return;
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

    private String setLoopLabel() {
        return "while_loop_" + labelCounter + ":";
    }

    private String callLoopLabel() {
        return "while_loop_" + labelCounter;
    }

    private String setEndLabel() {
        return "while_end_" + labelCounter + ":";
    }

    private String callEndLabel() {
        return "while_end_" + labelCounter;
    }

    @Override
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();

        code.add(setLoopLabel());

        // Evaluate the loop condition.
        code.addAll(expression.generatei386Code());
        code.add("cmp eax, 0; Check if expression returns false.");
        code.add("je " + callEndLabel() + "; Jump to end of loop if expr. is false.");

        // Evaluate the loop body.
        code.addAll(mainBody.generatei386Code());
        code.add("jmp " + callLoopLabel() + "; Jump to top of loop.");

        code.add(setEndLabel());

        return code;
    }
}
