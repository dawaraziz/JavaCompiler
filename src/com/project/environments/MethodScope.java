package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.structure.Name;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;

import java.util.ArrayList;

public class MethodScope extends Scope {
    public final String name;
    public final ASTHead ast;

    public final Type type;

    public final ArrayList<String> modifiers;
    public final ArrayList<Parameter> parameters;
    public final ASTHead bodyBlock;

    public final BlockScope startScope;

    MethodScope(final ASTHead method, final ClassScope classScope) {
        bodyBlock = method.getMethodBlock();
        modifiers = method.getMethodModifiers().get(0);
        parameters = method.getMethodParameters();
        parentScope = classScope;

        type = method.getMethodReturnType();
        name = method.getMethodName();
        ast = method;

        if (bodyBlock != null) {
            startScope = new BlockScope(bodyBlock, this);
        } else {
            startScope = null;
        }
    }

    MethodScope(final String name, final Type type, final ArrayList<String> modifiers,
                final ArrayList<Parameter> parameters) {
        this.name = name;
        this.type = type;
        this.modifiers = modifiers;
        this.parameters = parameters;

        ast = null;
        bodyBlock = null;
        startScope = null;
    }

    public boolean sameSignature(final MethodScope otherMethod) {
        if (this.parameters != null && otherMethod.parameters != null) {
            return (this.name.equals(otherMethod.name) && this.parameters.size() == otherMethod.parameters.size()
                    && this.parameters.containsAll(otherMethod.parameters)
                    && otherMethod.parameters.containsAll(this.parameters));
        }
        else if (this.parameters == null && otherMethod.parameters == null) {
            return (this.name.equals(otherMethod.name));
        }
        else return false;
    }

    @Override
    boolean isInitCheck(final String variableName) {
        if (parameters == null) return false;

        for (final Parameter parameter : parameters) {
            if (parameter.name.getSimpleName().equals(variableName)) {
                return true;
            }
        }

        return false;
    }

    void linkParameters() {
        if (!(parentScope instanceof ClassScope)) {
            System.err.println("Found method with non-class scope parent; aborting!");
            System.exit(42);
        }

        if (parameters == null) return;

        final ClassScope parent = (ClassScope) parentScope;

        for (final Parameter param : parameters) {

            // We don't care about primitives.
            if (param.isVariable()) {
                final Name paramType = param.type.name;

                // If the name has a package, it's already qualified.
                if (paramType.getPackageName() == null) {
                    param.type.name = parent.findImportedType(paramType.getSimpleName());
                }
            }
        }
    }
}
