package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;

public class ForScope extends Scope {
    private static final String LOCAL_VARIABLE_DECLARATION = "LOCALVARIABLEDECLARATION";

    Scope forInitScope;
    ASTHead forInitStatement;
    ASTHead forExpression;
    ASTHead forUpdate;

    ASTHead mainBody;
    Scope mainScope;

    ForScope(final ASTHead head, final Scope parentScope) {
        final ASTNode blockBody = head.unsafeGetHeadNode();
        this.ast = new ASTHead(blockBody);
        this.parentScope = parentScope;

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

        for (int i = blockBody.children.size() - 1; i >= 3; --i) {
            if (blockBody.children.get(i).lexeme.equals("(")
                    && blockBody.children.get(i - 2).lexeme.equals(";")
                    && blockBody.children.get(i - 1).kind == null) {
                forInitStatement = new ASTHead(blockBody.children.get(i - 1));

                if (forInitStatement.unsafeGetHeadNode().lexeme.equals(LOCAL_VARIABLE_DECLARATION)) {
                    forInitScope = new DefinitionScope(forInitStatement, null, this);
                }
            } else if (blockBody.children.get(i).lexeme.equals(";")
                    && blockBody.children.get(i - 2).lexeme.equals(";")
                    && blockBody.children.get(i - 1).kind == null) {
                forExpression = new ASTHead(blockBody.children.get(i - 1));
            } else if (blockBody.children.get(i).lexeme.equals(";")
                    && blockBody.children.get(i - 2).lexeme.equals(")")
                    && blockBody.children.get(i - 1).kind == null) {
                forUpdate = new ASTHead(blockBody.children.get(i - 1));
            }
        }
    }

    @Override
    boolean isInitCheck(final String variableName) {
        if (forInitScope != null && forInitScope.name.equals(variableName)) {
            return true;
        } else {
            return parentScope.isInitCheck(variableName);
        }
    }
}
