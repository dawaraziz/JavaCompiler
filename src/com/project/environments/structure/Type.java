package com.project.environments.structure;

import com.project.environments.scopes.ClassScope;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;

import static com.project.environments.structure.Type.PRIM_TYPE.BYTE;
import static com.project.environments.structure.Type.PRIM_TYPE.CHAR;
import static com.project.environments.structure.Type.PRIM_TYPE.INT;
import static com.project.environments.structure.Type.PRIM_TYPE.SHORT;
import static com.project.environments.structure.Type.PRIM_TYPE.VAR;

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
    public final Kind literal_type;

    public Name name;
    public final boolean isArray;

    static public Type generateStringType() {
        final Type newType = new Type();
        newType.name = Name.generateStringName();
        return newType;
    }

    static public Type generateNullType() {
        final Type newType = new Type(Type.PRIM_TYPE.VAR);
        newType.name = new Name("null");
        return newType;
    }

    @Override
    public String toString() {
        return "Type {" +
                "prim_type=" + prim_type +
                ", literal_type=" + literal_type +
                ", name=" + name +
                ", isArray=" + isArray +
                '}';
    }

    private Type() {
        prim_type = VAR;
        isArray = false;
        literal_type = null;
    }

    public Type(final PRIM_TYPE prim_type) {
        this.prim_type = prim_type;

        if (prim_type == VAR) {
            this.name = new Name("null");
        } else {
            this.name = null;
        }

        this.isArray = false;
        this.literal_type = null;
    }

    public Type(final Kind literal_type) {
        this.literal_type = literal_type;
        this.prim_type = null;
        this.name = null;
        this.isArray = false;
    }

    public Type(final String simpleName, final Name packageName) {
        isArray = false;
        prim_type = VAR;
        this.literal_type = null;

        name = Name.generateFullyQualifiedName(simpleName, packageName);
    }

    public Type(final Type type, final boolean isArray) {
        this.prim_type = type.prim_type;
        this.name = type.name;
        this.isArray = isArray;
        this.literal_type = null;
    }

    public Type(final ArrayList<String> typeLexemes) {
        this.literal_type = null;
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

    public Kind typeToKind() {
        switch (prim_type) {
            case BOOLEAN:
                return Kind.BOOLEAN;
            case INT:
                return Kind.INT;
            case CHAR:
                return Kind.CHAR;
            case BYTE:
                return Kind.BYTE;
            case SHORT:
                return Kind.SHORT;
            case VAR:
                return Kind.EXPRESSIONNAME;
            case VOID:
                return Kind.VOID;
        }
        return Kind.NULL;
    }

    public boolean isString() {
        return prim_type == VAR
                && name.getSimpleName().equals("String");
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

        if (this.literal_type == null) {
            if (this.name != null) {
                return this.prim_type == other.prim_type && this.isArray == other.isArray && this.name.equals(other.name);
            } else {
                return this.prim_type == other.prim_type && this.isArray == other.isArray;
            }
        } else {
            return this.literal_type == other.literal_type;
        }
    }

    public boolean isReferenceType() {
        return prim_type == VAR || isArray;
    }

    public void linkType(final ClassScope classScope) {
        // We don't care about primitive types or already qualified names.
        if (prim_type != VAR || name.getPackageName() != null) return;

        name = classScope.findImportedType(name.getSimpleName());
    }

    public static Type generateObjectType() {
        final Type retType = new Type();
        retType.name = Name.generateObjectExtendsName();
        return retType;
    }

    public boolean isNullType() {
        return generateNullType().name.equals(name);
    }

    public boolean isNumericType() {
        return prim_type == INT || prim_type == CHAR
                || prim_type == BYTE || prim_type == SHORT;
    }

    public boolean isSmallerNumericType(final Type type) {
        return (prim_type == BYTE && type.prim_type == BYTE)
                || (prim_type == SHORT && (type.prim_type == BYTE || type.prim_type == SHORT))
                || (prim_type == INT && type.isNumericType());
    }

}
