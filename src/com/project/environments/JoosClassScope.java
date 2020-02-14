package com.project.environments;

import com.project.ast.ASTHead;
import com.project.environments.structure.Name;

import java.util.ArrayList;

public class JoosClassScope extends Scope {
    public enum CLASS_TYPE {
        INTERFACE,
        CLASS
    }

    public final String name;
    public final ASTHead ast;
    public final CLASS_TYPE type;
    public final ArrayList<String> modifiers;
    public final Name packageName;
    public final ArrayList<JoosImportScope> imports;
    public final ArrayList<JoosMethodScope> methodTable;
    public final ArrayList<JoosFieldScope> fieldTable;

    public JoosClassScope(final String name, final ASTHead ast) {
        this.name = name;
        this.ast = ast;
        this.packageName = ast.getPackageName();
        this.imports = ast.getImports(this);
        this.modifiers = ast.getClassModifiers();

        final ASTHead classDeclaration = ast.getClassDeclaration();

        this.type = classDeclaration.getClassType();

        fieldTable = new ArrayList<>();
        generateFieldTable();

        for (int i = 0; i < fieldTable.size(); ++i) {
            for (int j = i + 1; j < fieldTable.size(); ++j) {
                if (fieldTable.get(i).name.equals(fieldTable.get(j).name)) {
                    System.err.println("Found duplicate field in same class.");
                    System.exit(42);
                }
            }
        }

        methodTable = new ArrayList<>();
        generateMethodTable();
    }

    private void generateFieldTable() {
        final ArrayList<ASTHead> fields = ast.getFieldNodes();
        for (final ASTHead field : fields) {
            fieldTable.add(new JoosFieldScope(field, this));
        }
    }

    private void generateMethodTable() {
        final ArrayList<ASTHead> methods = ast.getMethodNodes();
        for (final ASTHead method : methods) {
            methodTable.add(new JoosMethodScope(method, this));
        }
    }
}
