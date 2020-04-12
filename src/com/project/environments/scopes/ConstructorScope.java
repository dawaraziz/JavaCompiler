package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.statements.DefinitionStatement;
import com.project.environments.statements.Statement;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;

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

    public final ArrayList<DefinitionStatement> stackIndexMap = new ArrayList<>();

    /**
     * Returns the stack offset from the ebp.
     */
    public int getStackOffset(final DefinitionStatement scope) {
        final int i = stackIndexMap.indexOf(scope);

        if (i == -1) {
            System.err.println("Could not identify scope's stack offset.");
            System.exit(42);
        }

        return -(12 + (i * 4));
    }

    public static int getThisStackOffset() {
        return 8;
    }

    ConstructorScope(final ASTHead constructor, final ClassScope classScope) {
        this.ast = constructor;
        this.parentScope = classScope;
        this.name = constructor.getConstructorName();
        this.type = classScope.type;

        modifiers = constructor.getConstructorModifiers().get(0);
        parameters = constructor.getMethodParameters(this);
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

    public boolean checkIdentifierAgainstParameters(final String identifier) {
        if (parameters == null) return false;

        return parameters.stream()
                .anyMatch(c -> c.name.equals(identifier));
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        if (parameters != null) parameters.forEach(c -> c.linkType(rootClass));
        if (body != null) body.linkTypesToQualifiedNames(rootClass);
    }

    public Parameter getParameterFromIdentifier(final String identifier) {
        if (parameters == null) return null;

        for (final Parameter parameter : parameters) {
            if (parameter.name.equals(identifier)) return parameter;
        }
        return null;
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

    boolean matchesParameters(final ArrayList<Type> argTypes) {
        final ArrayList<Type> parameterTypes = new ArrayList<>();

        if (argTypes.size() == 0 && (parameters == null || parameters.size() == 0)) {
            return true;
        } else if (parameters == null) {
            return false;
        }

        parameters.forEach(e -> parameterTypes.add(e.type));

        if (parameterTypes.size() != argTypes.size()) return false;

        for (int i = 0; i < argTypes.size(); ++i) {
            if (!parameterTypes.get(i).equals(argTypes.get(i))) return false;
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

    public void linkTypes(final ClassScope rootClass) {
        type.linkType(rootClass);

        if (parameters != null) parameters.forEach(c -> c.linkType(rootClass));
    }

    @Override
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();

        code.add("section .text ; Code for the constructor " + callLabel());
        code.add(setLabel());

        code.addAll(generatePrologueCode());

        if (getParentClass().getSuperConstructor() != null) {
            code.add("push dword [ebp + 8]");
            code.add("call " + getParentClass().getSuperConstructor().callLabel());
            code.add("add esp, 4");
        }

        code.addAll(getParentClass().generateFieldInitializationCode(8));

        code.add("");
        code.addAll(body.generatei386Code());
        code.add("");

        code.add(setEndLabel());

        code.addAll(generateEpilogueCode());

        code.add("ret");

        return code;
    }

    public String callEndLabel() {
        return callLabel() + "@end_method";
    }

    public String setEndLabel() {
        return callLabel() + "@end_method:";
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

    public boolean isAbstract() {
        return modifiers.contains("abstract");
    }
}

