package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;

public class BlockStatement extends Statement {
    final ArrayList<Statement> childScopes;

    BlockStatement(final ASTHead head, final Scope parentScope) {
        this.ast = head.getChild(1);
        this.parentScope = parentScope;
        this.name = null;
        this.type = null;

        final ArrayList<ASTHead> scopeStatements = new ArrayList<>();

        // Found block with no body; no point continuing.
        head.stripBracesFromBlock();

        if (head.getChildren().size() != 2
                || head.getChild(0).getKind() != Kind.CURLY_BRACKET_CLOSE
                || head.getChild(1).getKind() != Kind.CURLY_BRACKET_OPEN) {
            if (head.getChild(1).getLexeme().equals("BLOCKSTATEMENTS")) {
                scopeStatements.addAll(ast.getChildren());
            } else if (head.getKind() == null) {
                scopeStatements.add(ast);
            }
        }

        childScopes = Statement.generateStatementScope(scopeStatements, this);
    }

    @Override
    public boolean isVariableNameFree(final String variableName) {
        return parentScope.isVariableNameFree(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        childScopes.forEach(c -> c.linkTypesToQualifiedNames(rootClass));
    }

    @Override
    public void checkTypeSoundness() {
        childScopes.forEach(Scope::checkTypeSoundness);
    }
}
