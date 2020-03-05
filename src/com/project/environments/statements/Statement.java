package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.Scope;

import java.util.ArrayList;
import java.util.List;

public abstract class Statement extends Scope {

    static ArrayList<Statement> generateStatementScope
            (final List<ASTHead> statements, final Scope parentScope) {
        final ArrayList<Statement> childScopes = new ArrayList<>();

        if (statements == null) return childScopes;

        for (int i = statements.size() - 1; i >= 0; --i) {
            final ASTHead statement = statements.get(i);

            if (statement.isBlock()) {
                childScopes.add(new BlockStatement(statement, parentScope));
            } else if (statement.isDefinition()) {
                childScopes.add(new DefinitionStatement(statement,
                        statements.subList(0, i),
                        parentScope));
                break;
            } else if (statement.isEmptyStatement()) {
                childScopes.add(new EmptyStatement(statement, parentScope));
            } else if (statement.isExpressionStatement()) {
                childScopes.add(new ExpressionStatement(statement, parentScope));
            } else if (statement.isForStatement()) {
                childScopes.add(new ForStatement(statement, parentScope));
            } else if (statement.isIfStatement()) {
                childScopes.add(new IfStatement(statement, parentScope));
            } else if (statement.isReturnStatement()) {
                childScopes.add(new ReturnStatement(statement, parentScope));
            } else if (statement.isWhileStatement()) {
                childScopes.add(new WhileStatement(statement, parentScope));
            } else {
                System.err.println("Could not ID statement; Aborting!");
                System.exit(42);
            }
        }

        return childScopes;
    }

    public static Statement generateStatementScope(final ASTHead statement,
                                                   final Scope parentScope) {
        if (statement == null) return null;

        final ArrayList<ASTHead> nodes = new ArrayList<>();
        nodes.add(statement);
        return generateStatementScope(nodes, parentScope).get(0);
    }
}
