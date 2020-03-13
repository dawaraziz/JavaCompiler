package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.project.environments.ast.ASTHead.LOCAL_VARIABLE_DECLARATION_STATEMENT;
import static com.project.environments.ast.ASTHead.VARIABLE_DECLARATOR_ID;
import static com.project.environments.ast.ASTNode.lexemesToStringList;
import static com.project.environments.expressions.Expression.generateExpressionScope;

public class DefinitionStatement extends Statement {
    final Expression initialization;
    final ArrayList<Statement> statements;

    @Override
    public void checkReturnedTypes(Type type, HashMap<String, ClassScope> classmap) {
        for (Statement stmt : statements){
            System.out.println(stmt);
            stmt.checkReturnedTypes(type, classmap);
        }
        return;
    }

    @Override
    public void checkConditionals() {
        System.out.println("Will iterate through defintion Statment children: " + statements.size());
        for (Statement stmt : statements){
            System.out.println(stmt);
            stmt.checkConditionals();
        }
    }

    @Override
    public void checkReachability() {
        if (!in) {
            System.err.println("Found unreachable definition.");
            System.exit(42);
        }

        statements.forEach(Statement::checkReachability);
    }

    @Override
    public void assignReachability() {
        if (!in) {
            out = false;
            return;
        }

        if (statements.size() == 0) {
            out = in;
            return;
        }

        for (int i = 0; i < statements.size(); ++i) {
            final Statement curStatement = statements.get(i);
            if (i == 0) {
                curStatement.in = this.in;
            } else {
                curStatement.in = statements.get(i - 1).out;
            }

            curStatement.assignReachability();
        }

        out = statements.get(statements.size() - 1).out;
    }

    DefinitionStatement(final ASTHead head, final List<ASTHead> postStatements,
                        final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;

        final ASTHead declaration;
        if (head.getLexeme().equals(LOCAL_VARIABLE_DECLARATION_STATEMENT)) {
            declaration = head.getChild(1);
        } else {
            declaration = head;
        }

        type = new Type(lexemesToStringList(declaration.getChild(1).unsafeGetHeadNode().getLeafNodes()));

        name = declaration.unsafeGetHeadNode()
                .findNodesWithLexeme(VARIABLE_DECLARATOR_ID).get(0)
                .children.get(0).lexeme;

        if (parentScope.isVariableNameUsed(name)) {
            System.err.println("Encountered duplicate variable in scope.");
            System.exit(42);
        }

        if (declaration.getChild(0).getChildren().size() != 3) {
            System.err.println("JOOS does not allow variable declaration without initializer.");
            System.exit(42);
        }

        initialization = generateExpressionScope(declaration.getChild(0).getChild(0), this);
        statements = generateStatementScope(postStatements, this);
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        if (name.equals(variableName)) {
            return true;
        } else {
            return parentScope.isVariableNameUsed(variableName);
        }
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        type.linkType(rootClass);
        initialization.linkTypesToQualifiedNames(rootClass);
        statements.forEach(c -> c.linkTypesToQualifiedNames(rootClass));
    }

    @Override
    public void checkTypeSoundness() {
        // TODO: Uncomment when expression types are implemented.
//        if (!type.equals(initialization.type)) {
//            System.err.println("Definition initializer has wrong type.");
//            System.exit(42);
//        }

        statements.forEach(Scope::checkTypeSoundness);
    }

    public boolean checkIdentifier(final String identifier) {
        return identifier.equals(name);
    }
}
