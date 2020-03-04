package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;

import java.util.ArrayList;

public class BlockScope extends Scope {
    private final static String BLOCK_STATEMENTS = "BLOCKSTATEMENTS";

    public final ArrayList<ASTHead> statements;
    public final ArrayList<Scope> childScopes;

    BlockScope(final ASTHead head, final Scope parentScope) {
        final ASTNode blockBody = head.unsafeGetHeadNode().children.get(1);
        this.ast = new ASTHead(blockBody);
        this.parentScope = parentScope;
        statements = new ArrayList<>();
        childScopes = new ArrayList<>();

        ArrayList<ASTNode> scopeStatements = new ArrayList<>();
        if (blockBody.lexeme.equals(BLOCK_STATEMENTS)) {
            scopeStatements = blockBody.children;
        } else if (blockBody.kind == null) {
            scopeStatements.add(blockBody);
        } else {
            this.ast = null;
            return;
        }

        for (int i = scopeStatements.size() - 1; i >= 0; --i) {
            final ASTHead statement = new ASTHead(scopeStatements.get(i));

            if (statement.isBlock()) {
                childScopes.add(new BlockScope(statement, this));
            } else if (statement.isDefinition()) {

                final ArrayList<ASTNode> postStatements = new ArrayList<>();
                for (int j = 0; j < i; ++j) {
                    postStatements.add(scopeStatements.get(j));
                }

                childScopes.add(new DefinitionScope(statement, postStatements, this));

                break;
            } else if (statement.isIfStatement()) {
                childScopes.add(new IfScope(statement, this));
            } else if (statement.isWhileStatement()) {
                childScopes.add(new WhileScope(statement, this));
            } else if (statement.isForStatement()) {
                childScopes.add(new ForScope(statement, this));
            } else {
                statements.add(statement);
            }
        }
    }

    @Override
    boolean isInitCheck(final String variableName) {
        return parentScope.isInitCheck(variableName);
    }
}
