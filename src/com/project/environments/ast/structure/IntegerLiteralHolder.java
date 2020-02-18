package com.project.environments.ast.structure;

public class IntegerLiteralHolder {
    public enum ParentType {
        UNARY,
        OTHER
    }

    public final ParentType parentType;
    public final long value;

    public IntegerLiteralHolder(final ParentType parentType, final long value) {
        this.parentType = parentType;
        this.value = value;
    }
}
