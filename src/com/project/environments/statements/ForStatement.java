package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.expressions.LiteralExpression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

public class ForStatement extends Statement {
    public Scope forInit;
    Expression forExpression;
    Expression forUpdate;
    final Statement forBody;

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

    public ForStatement(final ASTHead head, final Scope parentScope) {
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
        // TODO: Uncomment when expression types are implemented.
//        if (forExpression != null && forExpression.type.prim_type != BOOLEAN) {
//            System.err.println("Encountered non-boolean for expression.");
//            System.exit(42);
//        }

        if (forInit != null) forInit.checkTypeSoundness();
        if (forExpression != null) forExpression.checkTypeSoundness();
        if (forUpdate != null) forUpdate.checkTypeSoundness();
        if (forBody != null) forBody.checkTypeSoundness();
    }
}
