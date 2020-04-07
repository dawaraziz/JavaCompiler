package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

public class FieldAccessExpression extends Expression {
    Expression expressionName;
    Expression primary;

    public FieldAccessExpression(ASTHead head, Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        expressionName = generateExpressionScope(head.getChild(0), this);
        primary = generateExpressionScope(head.getChild(2), this);
    }

    @Override
    public boolean isVariableNameUsed(String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        expressionName.linkTypesToQualifiedNames(rootClass);
        primary.linkTypesToQualifiedNames(rootClass);

        ((NameExpression) expressionName).classifyExpressionNameWithType(primary.type);

        this.type = expressionName.type;
    }

    @Override
    public void checkTypeSoundness() {

    }

    @Override
    public String code() {
        StringBuilder assembly = new StringBuilder();
        assembly.append(expressionName.code());
        assembly.append("\n");
        assembly.append("push eax;");
        assembly.append("\n");
        assembly.append(primary.code());
        assembly.append("\n");
        assembly.append("pop ebx;");
        assembly.append("\n");

        // TODO:

        return assembly.toString();
    }
}
