package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.structure.Name;

import java.util.ArrayList;
import java.util.Objects;

public class ClassScope extends Scope {
    public enum CLASS_TYPE {
        INTERFACE,
        CLASS
    }

    public final String name;
    public final ASTHead ast;
    public final CLASS_TYPE type;
    public final Name packageName;

    public final ArrayList<String> modifiers;
    public final ArrayList<ImportScope> imports;

    public final ArrayList<Name> implementsTable;
    public final Name extendsName;

    public final ArrayList<MethodScope> methodTable;
    public final ArrayList<ConstructorScope> constructorTable;
    public final ArrayList<FieldScope> fieldTable;


    public ClassScope(final String name, final ASTHead ast) {
        this.name = name;
        this.ast = ast;
        this.packageName = ast.getPackageName();
        this.imports = ast.getImports(this);
        this.modifiers = ast.getClassModifiers();

        final ASTHead classDeclaration = ast.getClassDeclaration();

        this.type = classDeclaration.getClassType();

        implementsTable = classDeclaration.getClassInterfaces();
        extendsName = classDeclaration.getClassSuperClass();

        fieldTable = new ArrayList<>();
        generateFieldTable();
        checkDuplicateFields();

        methodTable = new ArrayList<>();
        generateMethodTable();

        constructorTable = new ArrayList<>();
        generateConstructorTable();
    }

    private void generateConstructorTable() {
        final ArrayList<ASTHead> constructors = ast.getConstructorNodes();
        for (final ASTHead constructor : constructors) {
            constructorTable.add(new ConstructorScope(constructor, this));
        }
    }

    private void generateFieldTable() {
        final ArrayList<ASTHead> fields = ast.getFieldNodes();
        for (final ASTHead field : fields) {
            fieldTable.add(new FieldScope(field, this));
        }
    }

    private void generateMethodTable() {
        final ArrayList<ASTHead> methods = ast.getMethodNodes();
        for (final ASTHead method : methods) {
            methodTable.add(new MethodScope(method, this));
        }
    }

    private void checkDuplicateFields() {
        for (int i = 0; i < fieldTable.size(); ++i) {
            for (int j = i + 1; j < fieldTable.size(); ++j) {
                if (fieldTable.get(i).name.equals(fieldTable.get(j).name)) {
                    System.err.println("Found duplicate field in same class.");
                    System.exit(42);
                }
            }
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ClassScope that = (ClassScope) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(packageName, that.packageName);
    }

    @Override
    boolean isInitCheck(final String variableName) {
        return false;
    }
}
