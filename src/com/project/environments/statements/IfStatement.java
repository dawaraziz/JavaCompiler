package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

import java.util.HashMap;

import static com.project.environments.expressions.Expression.generateExpressionScope;

public class IfStatement extends Statement {
    public final Expression expression;
    public final Statement ifBody;
    public final Statement elseBody;

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
    public void checkReturnedTypes(Type type, HashMap<String, ClassScope> classmap) {
        ifBody.checkReturnedTypes(type, classmap);
        if (elseBody != null) {
            elseBody.checkReturnedTypes(type, classmap);
        }
        return;
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
        // TODO: Uncomment when expression types are implemented.
//        if (expression.type.prim_type != BOOLEAN) {
//            System.err.println("Encountered non-boolean if expression.");
//            System.exit(42);
//        }

        expression.checkTypeSoundness();
        ifBody.checkTypeSoundness();
        if (elseBody != null) elseBody.checkTypeSoundness();
    }

    //Generate the assembly code
    public String code() {
        this.uniqueCount++;
        String uniqueID = String.valueOf(uniqueCount);
        StringBuilder assembly = new StringBuilder();
        assembly.append(expression.code());
        assembly.append("cmp eax, 0; compare value returned from if in eax to 0\n");
        assembly.append("je else" + uniqueID + "; jump to the else statement\n");
        assembly.append(ifBody.code());
        assembly.append("jmp end" + uniqueID + ";\n");
        assembly.append("else" + uniqueID + ": \n" + elseBody.code());
        assembly.append("end" + uniqueID + ": \n");
        return assembly.toString();
        }
}
