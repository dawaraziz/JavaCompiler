package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Statement extends Scope {

    // Note these are boolean, but we DO NOT HAVE TRUE.
    // Rather, we state true as "maybe" and false as "false".
    public boolean in; // Can the statement begin execution?
    public boolean out; // Can the statement end execution?

    abstract public String code();
    abstract public void assignReachability();
    abstract public void checkReachability();
    public void checkConditionals() { System.out.println("Exited Here"); return; };
    public void checkReturnedTypes(Type type, HashMap<String, ClassScope> classmap) { return; }

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
