package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

public class TypeExpression extends Expression {
    private final boolean isThis;

    private final Kind literalKind;
    private final String literalValue;

    public Kind evaluatesTo(){
        System.out.println("TYPEEXPRESSION: " + literalKind + " : interest " + literalValue);
        return booleanOrKind(literalKind);
    }

    TypeExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        isThis = head.getLexeme().equals("this");

        if (isThis) {
            literalKind = null;
            literalValue = head.getLexeme();
            return;
        }

        final ASTHead headType = ast.getChild(0);
        literalValue = headType.getLexeme();
        literalKind = headType.getKind();
    }


    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        if (isThis) {
            type = getParentClass().generateType();
            return;
        }

        switch (literalValue) {
            case "int":
                type = new Type(Type.PRIM_TYPE.INT);
                break;
            case "String":
            case "java.lang.String":
                type = Type.generateStringType();
                break;
            case "char":
                type = new Type(Type.PRIM_TYPE.CHAR);
                break;
            case "byte":
                type = new Type(Type.PRIM_TYPE.BYTE);
                break;
            case "boolean":
                type = new Type(Type.PRIM_TYPE.BOOLEAN);
                break;
            case "short":
                type = new Type(Type.PRIM_TYPE.SHORT);
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
