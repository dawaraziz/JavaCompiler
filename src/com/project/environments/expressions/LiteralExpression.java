package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;

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
    public Kind evaluatesTo(){
        System.out.println("The kind is: " + literal.getKind());
        if (literal.getKind() == FALSE || literal.getKind() == TRUE){
            return BOOLEAN;
        }
        return literal.getKind();
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

    @Override
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();

        switch (literal.getKind()) {
            case INTEGER_LITERAL:
                code.add("mov dword eax, " + literal.getLexeme());
                break;
            case STRING_LITERAL:
                // TODO:
                break;
            case CHARACTER_LITERAL:
                final int charInt;

                if (literal.getLexeme().length() == 1) {
                    charInt = literal.getLexeme().charAt(0);
                } else if (literal.getLexeme().length() == 3) {
                    charInt = literal.getLexeme().charAt(1);
                } else {
                    charInt = 0;
                    System.err.println("Found char literal with 2+ chars?");
                    System.exit(42);
                }

                code.add("mov dword eax, " + charInt);
                break;
            case NULL:
            case FALSE:
                code.add("mov eax, 0");
                break;
            case TRUE:
                code.add("mov eax, 1");
                break;
            default:
                System.err.println("Could not ID literal!");
                System.exit(42);
        }

        return code;
    }
}

