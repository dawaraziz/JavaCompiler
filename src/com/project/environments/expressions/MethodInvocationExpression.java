package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.MethodScope;
import com.project.environments.scopes.Scope;

import java.util.ArrayList;

public class MethodInvocationExpression extends Expression {
    final Expression methodName;
    final Expression argumentExpression;

    MethodInvocationExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.methodName = generateExpressionScope(head.getChild(head.getChildren().size() - 1), this);
        this.name = null;

        if (ast.getChildren().size() > 4) {
            argumentExpression = generateExpressionScope(head.generateMethodSubHead(), this);
        } else if (ast.getChildren().size() == 4) {
            argumentExpression = generateExpressionScope(head.getChild(1), this);
        } else {
            argumentExpression = null;
        }
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        if (argumentExpression != null) this.argumentExpression.linkTypesToQualifiedNames(rootClass);
        this.methodName.linkTypesToQualifiedNames(rootClass);


        final ArrayList<Expression> arguments = new ArrayList<>();
        if (argumentExpression instanceof ArgumentListExpression) {
            arguments.addAll(((ArgumentListExpression) argumentExpression).arguments);
        } else if (argumentExpression != null) {
            arguments.add(argumentExpression);
        }

        if (!(methodName instanceof NameExpression)) {
            System.err.println("Found method invocation without name.");
            System.exit(42);
        }

        final NameExpression name = (NameExpression) methodName;

        if (!name.isMethodName()) {
            System.err.println("Found method invocation with non-method name.");
            System.exit(42);
        }

        final ClassScope containingScope;
        final String identifier = name.getNameLexeme();
        final String qualifier = name.getQualifierName();

        if (name.getQualifierType() != null
                && name.getQualifierType().isReferenceType()) {
            containingScope = getParentClass().getClassFromPackage(
                    name.getQualifierType().name.getPackageString(),
                    name.getQualifierType().name.getSimpleName());
        } else {
            containingScope = getParentClass();
        }

        // Simple name case.
        final MethodScope resolvedMethod;
        if (qualifier == null) {
            resolvedMethod = containingScope.getMethodWithIdentifierAndParameters(identifier, arguments);

            if (resolvedMethod != null) {
                type = resolvedMethod.type;
                return;
            }
        } else {

            // Qualified name case.
            resolvedMethod = containingScope.getMethodWithIdentifierAndParameters(identifier, arguments);

            if (resolvedMethod != null) {
                type = resolvedMethod.type;
                return;
            }
        }
    }

    @Override
    public void checkTypeSoundness() {
        this.methodName.checkTypeSoundness();
        if (this.argumentExpression != null) this.argumentExpression.checkTypeSoundness();
        // TODO:
    }
}
