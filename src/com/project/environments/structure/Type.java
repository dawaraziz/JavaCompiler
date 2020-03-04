package com.project.environments.structure;

import com.project.environments.ClassScope;

import java.util.ArrayList;

public class Type {
    public enum PRIM_TYPE {
        BOOLEAN,
        INT,
        CHAR,
        BYTE,
        SHORT,
        VOID,
        VAR
    }

    static final private String INT_LEXEME = "int";
    static final private String BYTE_LEXEME = "byte";
    static final private String SHORT_LEXEME = "short";
    static final private String CHAR_LEXEME = "char";
    static final private String BOOLEAN_LEXEME = "boolean";
    static final private String VOID_LEXEME = "void";

    public final PRIM_TYPE prim_type;
    public Name name;
    public final boolean isArray;

    public Type(final ArrayList<String> typeLexemes) {
        if (typeLexemes.contains("[") && typeLexemes.contains("]")) {
            isArray = true;
            typeLexemes.remove("[");
            typeLexemes.remove("]");
        } else {
            isArray = false;
        }

        if (typeLexemes.size() == 1) {
            final String lexeme = typeLexemes.get(0);

            switch (lexeme) {
                case INT_LEXEME:
                    prim_type = PRIM_TYPE.INT;
                    name = null;
                    break;
                case BYTE_LEXEME:
                    prim_type = PRIM_TYPE.BYTE;
                    name = null;
                    break;
                case SHORT_LEXEME:
                    prim_type = PRIM_TYPE.SHORT;
                    name = null;
                    break;
                case CHAR_LEXEME:
                    prim_type = PRIM_TYPE.CHAR;
                    name = null;
                    break;
                case BOOLEAN_LEXEME:
                    prim_type = PRIM_TYPE.BOOLEAN;
                    name = null;
                    break;
                case VOID_LEXEME:
                    prim_type = PRIM_TYPE.VOID;
                    name = null;
                    break;
                default:
                    prim_type = PRIM_TYPE.VAR;
                    name = new Name(lexeme);
                    break;
            }
        } else {
            prim_type = PRIM_TYPE.VAR;
            name = new Name(typeLexemes);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof Type)) {
            return false;
        } else if (obj == this) {
            return true;
        }

        final Type other = (Type) obj;

        if (this.name != null) {
            return this.prim_type == other.prim_type && this.isArray == other.isArray && this.name.equals(other.name);
        } else {
            return this.prim_type == other.prim_type && this.isArray == other.isArray;
        }
    }

    public void linkType(final ClassScope classScope) {
        if (prim_type == PRIM_TYPE.VAR) {
            if (name.getPackageName() == null) {
                name = classScope.findImportedType(name.getSimpleName());
            }
        }
    }
}
