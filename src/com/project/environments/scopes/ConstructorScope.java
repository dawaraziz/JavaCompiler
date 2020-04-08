package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.statements.Statement;
import com.project.environments.structure.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static com.project.environments.scopes.MethodScope.generateEpilogueCode;
import static com.project.environments.scopes.MethodScope.generatePrologueCode;
import static com.project.environments.structure.Name.generateFullyQualifiedName;

public class ConstructorScope extends Scope {
    public final ArrayList<String> modifiers;
    public final ArrayList<Parameter> parameters;
    public final Statement body;

    ConstructorScope(final ASTHead constructor, final ClassScope classScope) {
        this.ast = constructor;
        this.parentScope = classScope;
        this.name = constructor.getConstructorName();
        this.type = classScope.type;

        modifiers = constructor.getConstructorModifiers().get(0);
        parameters = constructor.getMethodParameters();
        body = Statement.generateStatementScope(constructor.getConstructorBlock(), this);
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

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        if (parameters == null) return;

        parameters.forEach(c -> c.linkType(rootClass));
    }

    @Override
    public void checkTypeSoundness() {
        body.checkTypeSoundness();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ConstructorScope that = (ConstructorScope) o;

        if (this.parameters == null && that.parameters == null) return true;
        if (this.parameters == null || that.parameters == null) return false;

        if (this.parameters.size() != that.parameters.size()) return false;

        for (int i = 0; i < this.parameters.size(); ++i) {
            if (!this.parameters.get(i).equals(that.parameters.get(i))) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameters);
    }

    public void assignReachability() {
        if (body != null) body.assignReachability();
    }

    public void checkReachability() {
        if (body != null) body.checkReachability();
    }

    void checkConditionals() {
        if (body != null) {
            System.out.println(body);
            body.checkConditionals();
        }
    }

    public void checkReturnedTypes(final HashMap<String, ClassScope> classmap) {
        if (body != null) {
            System.out.println("Constructor Scope body: " + body);
            body.checkReturnedTypes(type, classmap);
        }
    }

    @Override
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();

        code.add("section .text ; Code for the constructor " + callLabel());

        code.addAll(generatePrologueCode());

        // TODO: Call super constructor.

        // TODO: Field initialization expressions.

        code.add("");
        body.generatei386Code();
        code.add("");

        code.addAll(generateEpilogueCode());

        code.add("ret");

        return code;
    }

    public String setLabel() {
        return callLabel() + ":";
    }

    public String callLabel() {
        final ClassScope classScope = ((ClassScope) parentScope);

        final String label;
        if (classScope == null) {
            label = "java.lang.Object";
        } else {
            label = generateFullyQualifiedName(classScope.name,
                    classScope.packageName).getQualifiedName();
        }

        final StringBuilder argLabel = new StringBuilder();
        if (parameters != null) {
            for (final Parameter parameter : parameters) {
                argLabel.append("_");
                argLabel.append(parameter.toString());
            }
        }

        return label + "_" + name + argLabel.toString();
    }

    public String generateExternStatement() {
        return "extern " + callLabel();
    }
}

