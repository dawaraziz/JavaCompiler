package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.expressions.LiteralExpression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

import java.util.HashMap;

public class WhileStatement extends Statement {

    final Expression expression;
    final Statement mainBody;

    @Override
    public void checkConditionals() {
        // expression must evaluate to boolean
        System.out.println("Expression type is: " + expression);
        if (!(expression.evaluatesTo() == Kind.BOOLEAN)){
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

    //Generate the assembly code
    public String code() {
        this.uniqueCount++;
        String uniqueID = String.valueOf(uniqueCount);
        StringBuilder assembly = new StringBuilder();
        String loopID = "loop" + uniqueID;
        String endID = "end" + uniqueID;

        //Am i missing initialization of a variable in the for loop?

        // Start of loop assembly
        assembly.append(loopID + ": \n");

        // Evaluate the while loop condition
        assembly.append(expression.code());
        assembly.append("cmp eax, 0; evaluate value returned from for check in eax\n");
        assembly.append("je " + endID + "; jump to end of for loop\n");

        // For loop body
        assembly.append(mainBody.code());
        assembly.append("jmp " +  loopID + "; jump to top of loop \n");


        // code after for loop
        assembly.append(endID + ": \n");
        return assembly.toString();
    }
}
