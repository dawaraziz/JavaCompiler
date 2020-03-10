package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

public class LiteralExpression extends Expression {

    public enum LITERAL_TYPE {
        TRUE,
        FALSE,
        NULL,
        STRING_LITERAL,
        INTEGER_LITERAL,
        CHARACTER_LITERAL
    }

    final ASTHead literal;
//    final LITERAL_TYPE literal_type;


    LiteralExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        literal = head.getChild(0);

//        String lexeme = literal.getLexeme();
//
//        if (lexeme.equals("INTEGER_LITERAL")) this.literal_type = LITERAL_TYPE.INTEGER_LITERAL;
//        else if (lexeme.equals("STRING_LITERAL")) this.literal_type = LITERAL_TYPE.STRING_LITERAL;
//        else if (lexeme.equals("CHARACTER_LITERAL")) this.literal_type = LITERAL_TYPE.CHARACTER_LITERAL;
//        else if (lexeme.equals("NULL")) this.literal_type = LITERAL_TYPE.NULL;
//        else if (lexeme.equals("TRUE")) this.literal_type = LITERAL_TYPE.TRUE;
//        else if (lexeme.equals("FALSE")) this.literal_type = LITERAL_TYPE.FALSE;
//        else {
//            this.literal_type = null;
//            System.err.println("Could not ID literal!");
//            System.exit(42);
//        }
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

