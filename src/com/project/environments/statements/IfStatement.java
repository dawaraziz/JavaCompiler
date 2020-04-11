package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;
import java.util.HashMap;

import static com.project.environments.expressions.Expression.generateExpressionScope;

public class IfStatement extends Statement {
    public final Expression expression;
    private final Statement ifBody;
    private final Statement elseBody;

    private static long labelCounter = 0;

    @Override
    public void checkConditionals() {
        // expression must evaluate to boolean
        expression.ast.printAST();
        System.out.println("Expression type is: " + expression);
        if (!(expression.evaluatesTo() == Kind.BOOLEAN)){
            System.err.println("If Statement does not evaluate to a boolean");
            System.exit(42);
        }
    }

    @Override
    public void checkReturnedTypes(final Type type, final HashMap<String, ClassScope> classmap) {
        ifBody.checkReturnedTypes(type, classmap);
        if (elseBody != null) {
            elseBody.checkReturnedTypes(type, classmap);
        }
    }

    @Override
    public void assignReachability() {
        if (elseBody == null) {
            out = in;

            ifBody.in = this.in;
            ifBody.assignReachability();
        } else {
            ifBody.in = this.in;
            ifBody.assignReachability();

            elseBody.in = this.in;
            elseBody.assignReachability();

            this.out = ifBody.out || elseBody.out;
        }
    }

    @Override
    public void checkReachability() {
        if (!in) {
            System.err.println("Found unreachable if statement.");
            System.exit(42);
        }

        ifBody.checkReachability();
        if (elseBody != null) elseBody.checkReachability();
    }

    IfStatement(final ASTHead head, final Scope parentScope) {
        System.out.println("MADE IF STATEMENT: ");
        head.printAST();
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        final boolean hasElse = head.getChildren().size() > 5;

        expression = generateExpressionScope(head.getChild(head.getChildren().size() - 3),
                this);
        ifBody = generateStatementScope(head.getChild(head.getChildren().size() - 5),
                this);

        if (hasElse) {
            if (head.getChildren().size() == 7) {
                elseBody = generateStatementScope(head.getChild(0), this);
            } else {
                elseBody = generateStatementScope(head.generateIfSubHead(), this);
            }
        } else {
            elseBody = null;
        }
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return parentScope.isVariableNameUsed(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        expression.linkTypesToQualifiedNames(rootClass);
        ifBody.linkTypesToQualifiedNames(rootClass);
        if (elseBody != null) elseBody.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        expression.checkTypeSoundness();
        ifBody.checkTypeSoundness();
        if (elseBody != null) elseBody.checkTypeSoundness();
    }

    @Override
    public ArrayList<String> generatei386Code() {
        labelCounter += 1;

        final long count = labelCounter;

        final ArrayList<String> code = new ArrayList<>();

        // Check the if expression.
        code.addAll(expression.generatei386Code());
        code.add("cmp eax, 0 ; Check if expression returns false.");
        code.add("je " + callElseLabel(count) + "; Jump to else if expr. is false.");

        // Generates the if body code.
        code.addAll(ifBody.generatei386Code());
        code.add("jmp " + callEndLabel(count));

        // Generate the else code if it exists.
        // Note that elseLabel = endLabel if it doesn't.
        code.add(setElseLabel(count));
        if (elseBody != null) {
            code.addAll(elseBody.generatei386Code());
        }

        // Sets the end label.
        code.add(setEndLabel(count));

        return code;
    }

    private String setElseLabel(final long count) {
        return "if_else_" + count + ":";
    }

    private String callElseLabel(final long count) {
        return "if_else_" + count;
    }

    private String setEndLabel(final long count) {
        return "if_end_" + count + ":";
    }

    private String callEndLabel(final long count) {
        return "if_end_" + count;
    }
}
