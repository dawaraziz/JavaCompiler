package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

import static com.project.scanner.structure.Kind.*;
import static com.project.scanner.structure.Kind.FALSE;

public class LiteralExpression extends Expression {

    final ASTHead literal;

    public boolean isFalse() {
        return literal.getKind() == FALSE;
    }

    public boolean isTrue() {
        return literal.getKind() == TRUE;
    }

    LiteralExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        literal = head.getChild(0);
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        switch (literal.getKind()) {
            case INTEGER_LITERAL:
                type = new Type(Type.PRIM_TYPE.INT);
                break;
            case STRING_LITERAL:
                type = Type.generateStringType();
                break;
            case CHARACTER_LITERAL:
                type = new Type(Type.PRIM_TYPE.CHAR);
                break;
            case NULL:
                type = new Type(Type.PRIM_TYPE.VAR);
                break;
            case TRUE:
            case FALSE:
                type = new Type(Type.PRIM_TYPE.BOOLEAN);
                break;
            default:
                System.err.println("Could not ID literal!");
                System.exit(42);
        }
    }

    @Override
    public void checkTypeSoundness() {
    }
}

