package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.ConstructorScope;
import com.project.environments.scopes.MethodScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;
import java.util.HashMap;

public class BlockStatement extends Statement {
    final ArrayList<Statement> childScopes;

    @Override
    public void checkReturnedTypes(Type type, HashMap<String, ClassScope> classmap) {
        for (Statement stmt : childScopes){
            System.out.println(stmt);
            stmt.checkReturnedTypes(type, classmap);
        }
        return;
    }


    @Override
    public void checkConditionals() {
        // expression must evaluate to boolean
        System.out.println("Will iterate through num children: " + childScopes.size());
        for (Statement stmt : childScopes){
            System.out.println(stmt);
            stmt.checkConditionals();
        }
    }

    @Override
    public void checkReachability() {
        if (!in) {
            System.err.println("Found unreachable block.");
            System.exit(42);
        }

        childScopes.forEach(Statement::checkReachability);
    }

    @Override
    public void assignReachability() {
        if (parentScope instanceof ConstructorScope || parentScope instanceof MethodScope) {
            in = true;
        }

        if (childScopes.size() == 0) {
            out = in;
            return;
        }

        for (int i = 0; i < childScopes.size(); ++i) {
            final Statement curStatement = childScopes.get(i);
            if (i == 0) {
                curStatement.in = this.in;
            } else {
                curStatement.in = childScopes.get(i - 1).out;
            }

            curStatement.assignReachability();
        }

        out = childScopes.get(childScopes.size() - 1).out;
    }

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
    public boolean isVariableNameUsed(final String variableName) {
        return parentScope.isVariableNameUsed(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        childScopes.forEach(c -> c.linkTypesToQualifiedNames(rootClass));
    }

    @Override
    public void checkTypeSoundness() {
        childScopes.forEach(Scope::checkTypeSoundness);
    }

    //Generate the assembly code
    public String code() {
//        this.uniqueCount++;
//        String uniqueID = String.valueOf(uniqueCount);
        StringBuilder assembly = new StringBuilder();
        // Get the assembly for all statements in the block
        for (Statement statement : childScopes){
            assembly.append(statement.code());
        }
        return assembly.toString();
    }
}
