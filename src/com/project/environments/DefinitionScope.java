package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;
import com.project.environments.structure.Type;

import java.util.ArrayList;

import static com.project.environments.ast.ASTHead.LOCAL_VARIABLE_DECLARATION_STATEMENT;
import static com.project.environments.ast.ASTHead.VARIABLE_DECLARATOR_ID;
import static com.project.environments.ast.ASTNode.lexemesToStringList;

public class DefinitionScope extends Scope {
    public final Type type;
    public final String name;
    public final ASTHead initialization;

    final ArrayList<ASTNode> postStatements;

    final ArrayList<Scope> childScopes;
    final ArrayList<ASTHead> statements;

    public DefinitionScope(final ASTHead headStatement, final ArrayList<ASTNode> postStatements,
                           final Scope parentScope) {
        this.ast = headStatement;
        this.parentScope = parentScope;
        this.postStatements = postStatements;

        ASTNode declaration = headStatement.unsafeGetHeadNode();

        if (headStatement.unsafeGetHeadNode().lexeme.equals(LOCAL_VARIABLE_DECLARATION_STATEMENT)) {
            declaration = headStatement.unsafeGetHeadNode().children.get(1);
        }

        type = new Type(lexemesToStringList(declaration.children.get(1).getLeafNodes()));
        name = declaration.findNodesWithLexeme(VARIABLE_DECLARATOR_ID).get(0)
                .children.get(0).lexeme;

        if (declaration.children.get(0).children.size() == 3) {
            initialization = new ASTHead(declaration.children.get(0).children.get(0));
        } else {
            initialization = null;
        }

        childScopes = new ArrayList<>();
        statements = new ArrayList<>();

        if (parentScope.isInitCheck(name)) {
            System.err.println("Encountered duplicate variable in scope.");
            System.exit(42);
        }

        if (postStatements == null) return;

        for (int i = postStatements.size() - 1; i >= 0; --i) {
            final ASTHead statement = new ASTHead(postStatements.get(i));

            if (statement.isBlock()) {
                childScopes.add(new BlockScope(statement, this));
            } else if (statement.isDefinition()) {

                final ArrayList<ASTNode> defPostStatements = new ArrayList<>();
                for (int j = 0; j < i; ++j) {
                    defPostStatements.add(postStatements.get(j));
                }

                childScopes.add(new DefinitionScope(statement, defPostStatements, this));

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
    boolean isInitCheck(String variableName) {
        if (this.name.equals(variableName)) {
            return true;
        } else {
            return parentScope.isInitCheck(variableName);
        }
    }
}
