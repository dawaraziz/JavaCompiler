package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.structure.Type;

import java.util.ArrayList;

import static com.project.environments.structure.Type.PRIM_TYPE.VAR;

public class FieldScope extends Scope {
    public final String name;
    public final ASTHead ast;

    public final Type type;

    public final ArrayList<String> modifiers;
    public final ASTHead initializer;

    public FieldScope(final ASTHead head, final ClassScope parentScope) {
        this.ast = head;

        final ArrayList<ArrayList<String>> modifiers = head.getFieldModifiers();

        if (modifiers.size() != 1) {
            System.err.println("Encountered field with incorrect modifiers; Aborting!");
            System.exit(42);
        }

        this.modifiers = modifiers.get(0);
        this.name = ast.getFieldName();

        this.initializer = ast.getFieldInitializer();

        this.type = ast.getFieldType();
        this.parentScope = parentScope;
    }

    @Override
    boolean isInitCheck(final String variableName) {
        return false;
    }

    void linkTypes() {
        if (!(parentScope instanceof ClassScope)) {
            System.err.println("Found field with non-class scope parent; aborting!");
            System.exit(42);
        }

        if (type == null) {
            System.err.println("Found field with null type; aborting!");
            System.exit(42);
        }

        final ClassScope parent = (ClassScope) parentScope;

        // We don't care about primitives.
        if (type.prim_type == VAR) {

            // If the name has a package, it's already qualified.
            if (type.name.getPackageName() == null) {
                type.name = parent.findImportedType(type.name.getSimpleName());
            }
        }
    }
}
