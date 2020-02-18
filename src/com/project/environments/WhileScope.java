package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;

public class WhileScope extends Scope {

    public ASTHead expression;

    public ASTHead mainBody;
    public Scope mainScope;

    WhileScope(final ASTHead head, final Scope parentScope) {
        final ASTNode blockBody = head.unsafeGetHeadNode();
        this.ast = new ASTHead(blockBody);
        this.parentScope = parentScope;

        expression = new ASTHead(blockBody.children.get(2));
        mainBody = new ASTHead(blockBody.children.get(0));

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
    }

    @Override
    boolean isInitCheck(final String variableName) {
        return parentScope.isInitCheck(variableName);
    }
}
