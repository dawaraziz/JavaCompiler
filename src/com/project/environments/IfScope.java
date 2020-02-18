package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;

public class IfScope extends Scope {
    private static final String ELSE = "else";

    public ASTHead expression;

    public ASTHead mainBody;
    public Scope mainScope;

    public ASTHead elseBody;
    public Scope elseScope;

    IfScope(final ASTHead head, final Scope parentScope) {
        final ASTNode blockBody = head.unsafeGetHeadNode();
        this.ast = new ASTHead(blockBody);
        this.parentScope = parentScope;

        final boolean hasElse = blockBody.getDirectChildrenWithLexemes(ELSE).size() == 1;

        if (hasElse) {
            expression = new ASTHead(blockBody.children.get(4));
            mainBody = new ASTHead(blockBody.children.get(2));
            elseBody = new ASTHead(blockBody.children.get(0));
        } else {
            expression = new ASTHead(blockBody.children.get(2));
            mainBody = new ASTHead(blockBody.children.get(0));
        }

        if (mainBody.isBlock()) {
            mainScope = new BlockScope(mainBody, this);
        } else if (mainBody.isDefinition()) {
            mainScope = new DefinitionScope(mainBody, null, this);
        } else if (mainBody.isIfStatement()) {
            mainScope = new IfScope(mainBody, this);
        } else if (mainBody.isWhileStatement()) {
            mainScope = new WhileScope(mainBody, this);
        } else if (mainBody.isForStatement()) {
            mainScope = new ForScope(mainBody, this);
        }

        if (!hasElse) return;

        if (elseBody.isBlock()) {
            elseScope = new BlockScope(elseBody, this);
        } else if (elseBody.isDefinition()) {
            elseScope = new DefinitionScope(elseBody, null, this);
        } else if (elseBody.isIfStatement()) {
            elseScope = new IfScope(elseBody, this);
        } else if (elseBody.isWhileStatement()) {
            elseScope = new WhileScope(elseBody, this);
        } else if (elseBody.isForStatement()) {
            elseScope = new ForScope(elseBody, this);
        }
    }

    @Override
    boolean isInitCheck(final String variableName) {
        return parentScope.isInitCheck(variableName);
    }
}
