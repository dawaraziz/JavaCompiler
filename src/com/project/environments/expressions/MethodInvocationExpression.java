package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.MethodScope;
import com.project.environments.scopes.Scope;

import java.util.ArrayList;

import static com.project.scanner.structure.Kind.DOT;
import static com.project.scanner.structure.Kind.PAREN_OPEN;

public class MethodInvocationExpression extends Expression {
    final Expression primaryExpression;
    final Expression methodName;
    final Expression argumentExpression;

    MethodInvocationExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        if (ast.getChild(ast.getChildren().size() - 2).getKind() == DOT) {
            if (ast.getChildren().size() > 6) {
                int firstParens = -1;
                for (int i = 0; i < head.getChildren().size(); ++i) {
                    if (head.getChild(i).getKind().equals(PAREN_OPEN)) {
                        firstParens = i;
                        break;
                    }
                }

                if (firstParens == -1) {
                    System.err.println("Could not identify end of params; aborting!");
                    System.exit(42);
                }

                argumentExpression = generateExpressionScope(head.generateSubHead(1,
                        firstParens, "ARGUMENTLIST"), this);
                methodName = generateExpressionScope(head.getChild(firstParens + 1), this);
                primaryExpression = generateExpressionScope(head
                        .generateSubHead(firstParens + 3,
                                head.getChildren().size(), "METHODINVOCATION"), this);
            } else if (ast.getChildren().size() == 6) {
                primaryExpression = generateExpressionScope(head.getChild(head.getChildren().size() - 1), this);
                methodName = generateExpressionScope(head.getChild(head.getChildren().size() - 3), this);
                argumentExpression = generateExpressionScope(head.getChild(1), this);
            } else {
                primaryExpression = generateExpressionScope(head.getChild(head.getChildren().size() - 1), this);
                methodName = generateExpressionScope(head.getChild(head.getChildren().size() - 3), this);
                argumentExpression = null;
            }
        } else {
            methodName = generateExpressionScope(head.getChild(head.getChildren().size() - 1), this);
            if (ast.getChildren().size() > 4) {
                argumentExpression = generateExpressionScope(head.generateMethodSubHead(), this);
            } else if (ast.getChildren().size() == 4) {
                argumentExpression = generateExpressionScope(head.getChild(1), this);
            } else {
                argumentExpression = null;
            }
            primaryExpression = null;
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
        if (primaryExpression != null) primaryExpression.linkTypesToQualifiedNames(rootClass);


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

        final String qualifier;
        if (primaryExpression != null) {
            if (name.getQualifierType() != null) {
                System.err.println("Found primary method invocation with qualified name.");
                System.exit(42);
            }
            qualifier = primaryExpression.type.name.getQualifiedName();
        } else {
            qualifier = name.getQualifierName();
        }

        if (primaryExpression != null) {
            containingScope = getParentClass().getClassFromPackage(
                    primaryExpression.type.name.getPackageString(),
                    primaryExpression.type.name.getSimpleName());
        } else if (name.getQualifierType() != null
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

            System.err.println("Could not determine type of non-qualified method.");
            System.exit(42);
        } else {

            // Qualified name case.
            resolvedMethod = containingScope.getMethodWithIdentifierAndParameters(identifier, arguments);

            if (resolvedMethod != null) {
                type = resolvedMethod.type;
                return;
            }

            System.err.println("Could not determine type of qualified method.");
            System.exit(42);
        }
    }

    @Override
    public void checkTypeSoundness() {
        this.methodName.checkTypeSoundness();
        if (this.argumentExpression != null) this.argumentExpression.checkTypeSoundness();
        // TODO:
    }


    @Override
    public String code() {
        StringBuilder assembly = new StringBuilder();

        assembly.append(primaryExpression.code());
        assembly.append("\n");
        assembly.append("push eax;");
        assembly.append("\n");
        assembly.append(argumentExpression.code());
        assembly.append("\n");
        assembly.append("push eax;"); // multiple push for each argument?
        assembly.append("\n");

        return assembly.toString();
    }
}
