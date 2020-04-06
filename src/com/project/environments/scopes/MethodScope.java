package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.statements.Statement;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;

import java.util.ArrayList;
import java.util.HashMap;

import static com.project.Main.testMethod;
import static com.project.environments.structure.Name.generateFullyQualifiedName;
import static com.project.environments.structure.Type.PRIM_TYPE.INT;
import static com.project.environments.structure.Type.PRIM_TYPE.VOID;

public class MethodScope extends Scope {
    public final ArrayList<String> modifiers;
    public final ArrayList<Parameter> parameters;
    public final Statement body;

    MethodScope(final ASTHead method, final ClassScope classScope) {
        this.ast = method;
        this.parentScope = classScope;
        this.name = method.getMethodName();
        this.type = method.getMethodReturnType();

        modifiers = method.getMethodModifiers().get(0);
        parameters = method.getMethodParameters();
        body = Statement.generateStatementScope(method.getMethodBlock(), this);

        // If there is no test method, check if we are it.
        if (testMethod == null
                && name.equals("test")
                && modifiers.contains("static")
                && type.prim_type == INT) {
            testMethod = this;
        }
    }

    MethodScope(final String name, final Type type, final ArrayList<String> modifiers,
                final ArrayList<Parameter> parameters) {
        this.ast = null;
        this.parentScope = null;
        this.name = name;

        this.type = type;
        this.modifiers = modifiers;
        this.parameters = parameters;
        this.body = null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MethodScope that = (MethodScope) o;

        if (!this.name.equals(that.name)) return false;

        if (this.parameters == null && that.parameters == null) return true;

        if (this.parameters == null || that.parameters == null) return false;
        if (this.parameters.size() != that.parameters.size()) return false;
        for (int i = 0; i < this.parameters.size(); ++i) {
            if (!this.parameters.get(i).equals(that.parameters.get(i))) return false;
        }

        return true;
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        if (parameters == null) return false;

        for (final Parameter parameter : parameters) {
            if (parameter.name.equals(variableName)) {
                return true;
            }
        }

        return false;
    }

    public void linkTypes(ClassScope rootClass) {
        type.linkType(rootClass);

        if (parameters != null) parameters.forEach(c -> c.linkType(rootClass));
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        if (body != null) body.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        if (body != null) body.checkTypeSoundness();
    }

    public boolean checkIdentifierAgainstParameters(final String identifier) {
        if (parameters == null) return false;

        return parameters.stream()
                .anyMatch(c -> c.name.equals(identifier));
    }

    boolean checkIdentifier(final String identifier) {
        return this.name.equals(identifier);
    }

    public Parameter getParameterFromIdentifier(final String identifier) {
        if (parameters == null) return null;

        for (final Parameter parameter : parameters) {
            if (parameter.name.equals(identifier)) return parameter;
        }
        return null;
    }

    public void assignReachability() {
        if (body != null) body.assignReachability();
    }

    public void checkReachability() {
        if (body != null) {
            body.checkReachability();

            if (body.out && type.prim_type != VOID) {
                System.err.println("Found non-void method without return statement.");
                System.exit(42);
            }
        }
    }

    public void checkConditionals() {
        if (body != null) {
            System.out.println("Method Scope body: " + body);
            body.checkConditionals();
        }
    }

    public void checkReturnedTypes(HashMap<String, ClassScope> classmap) {
        if (body != null) {
            System.out.println("Method Scope body: " + body);
            body.checkReturnedTypes(type, classmap);
        }
    }

    public String setLabel() {
        final ClassScope classScope = ((ClassScope) parentScope);
        final String label = generateFullyQualifiedName(classScope.name,
                classScope.packageName).getQualifiedName();
        return label + "_" + name + ":";
    }

    public String callLabel() {
        final ClassScope classScope = ((ClassScope) parentScope);
        final String label = generateFullyQualifiedName(classScope.name,
                classScope.packageName).getQualifiedName();
        return label + "_" + name;
    }

    @Override
    public ArrayList<String> generatei386Code() {
        return new ArrayList<>();
    }
}
