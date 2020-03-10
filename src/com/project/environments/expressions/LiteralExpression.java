package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

public class LiteralExpression extends Expression {

    final ASTHead literal;
    public final Kind literal_kind;


    LiteralExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        literal = head.getChild(0);

        Kind kind = literal.getKind();

        if ((kind == Kind.INTEGER_LITERAL) || (kind == Kind.STRING_LITERAL) || (kind == Kind.CHARACTER_LITERAL) ||
        (kind == Kind.NULL) || (kind == Kind.TRUE) || (kind == Kind.FALSE)) {
            this.literal_kind = kind;
        }
        else {
            this.literal_kind = null;
            System.err.println("Could not ID literal!");
            System.exit(42);
        }
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
    }

    @Override
    public void checkTypeSoundness() {
    }
}

