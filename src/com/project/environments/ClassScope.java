package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.structure.Name;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import static com.project.environments.ImportScope.IMPORT_TYPE.SINGLE;

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
    public final HashSet<String> usedTypeNameStrings;
    public final ArrayList<Name> usedTypeNames; // Same thing as used TypeNameStrings but Name Objects

    public final ArrayList<Name> implementsTable;
    public ArrayList<Name> extendsTable;

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

        if (this.type == CLASS_TYPE.INTERFACE) {
            extendsTable = classDeclaration.getInterfaceSuperInterfaces();
        } else {
            extendsTable = classDeclaration.getClassSuperClass();
        }

        if (extendsTable != null) {
            for (int i = 0; i < extendsTable.size(); ++i) {
                for (final ImportScope importName : imports) {

                    final Name newName = importName.generateFullName(extendsTable.get(i));
                    if (newName != null) {
                        extendsTable.set(i, newName);
                    }
                }
            }

            if (new HashSet<>(extendsTable).size() < extendsTable.size()) {
                System.err.println("Found duplicate implements in same interface.");
                System.exit(42);
            }
        }

        if (this.type == CLASS_TYPE.CLASS) {
            if (!this.name.equals("Object")
                    || this.packageName == null
                    || !this.packageName.isJavaLang()) {
                if (extendsTable == null) {
                    extendsTable = new ArrayList<>();
                }
                if (extendsTable.size() == 0) {
                    extendsTable.add(Name.generateObjectExtendsName());
                }
            }
        }

        if (implementsTable != null) {
            for (int i = 0; i < implementsTable.size(); ++i) {
                for (final ImportScope importName : imports) {
                    final Name newName = importName.generateFullName(implementsTable.get(i));
                    if (newName != null) {
                        implementsTable.set(i, newName);
                    }
                }
            }

            if (new HashSet<Name>(implementsTable).size() < implementsTable.size()) {
                System.err.println("Found duplicate implements in same class.");
                System.exit(42);
            }
        }


        fieldTable = new ArrayList<>();
        generateFieldTable();
        checkDuplicateFields();

        methodTable = new ArrayList<>();
        generateMethodTable();

        constructorTable = new ArrayList<>();
        generateConstructorTable();

        this.usedTypeNameStrings = ast.getUsedTypeNames();
        this.usedTypeNames = new ArrayList<>();
        for (String s : usedTypeNameStrings){
            usedTypeNames.add(new Name(s));
        }
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

    public void generateObjectMethods(final ArrayList<MethodScope> objectMethods) {
        if (extendsTable != null && extendsTable.size() > 0) return;

        for (final MethodScope objectMethod : objectMethods) {
            final Boolean check = containsMethod(objectMethod);

            if (check == null) {
                System.err.println("Found interface with object method with bad return type.");
                System.exit(42);
            }

            if (!objectMethod.modifiers.contains("public")) continue;

            final ArrayList<String> newMods = new ArrayList<>(objectMethod.modifiers);
            newMods.add("abstract");

            if (!check) {
                methodTable.add(new MethodScope(
                        objectMethod.name,
                        objectMethod.type,
                        newMods,
                        objectMethod.parameters
                ));
            }
        }
    }

    private Boolean containsMethod(final MethodScope methodScope) {
        for (final MethodScope method : methodTable) {
            final boolean signatureMatch = method.sameSignature(methodScope);
            final boolean returnsMatch = method.type.equals(methodScope.type);

            if (signatureMatch && !returnsMatch) {
                return null;    // Same signature, different return type. BAD.
            } else if (signatureMatch) {
                return true;
            }
        }
        return false;
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

    public void qualifySupersAndInterfaces(final ArrayList<ClassScope> classTable) {
        if (extendsTable != null) {
            boolean isImported = false;
            for (int i = 0; i < extendsTable.size(); ++i) {
                final Name extendsName = extendsTable.get(i);

                if (isImportSuffix(extendsName)) {
                    isImported = true;
                    break;
                }

                for (final ClassScope classScope : classTable) {
                    if (classScope != null
                            && extendsName.getClassName().equals(classScope.name)) {
                        if (classScope.packageName != null) {
                            final Name qualifiedName = classScope.packageName.generateAppendedPackageName(extendsName.getClassName());
                            if (extendsName.checkPackageMatch(classScope.packageName)) {
                                isImported = true;
                                break;
                            } else if ((isOnDemandImportSuffix(classScope.packageName)
                                    || classScope.packageName.equals(this.packageName))
                                    &&  extendsName.containsSomePackageSuffix(classScope.packageName)) {
                                System.out.println("Changing " + i + " to: " + qualifiedName);
                                if (!classScope.packageName.isDefault()) {
                                    extendsTable.set(i, qualifiedName);
                                }
                                isImported = true;
                                break;
                            }
                        } else if (this.packageName == null) {
                            isImported = true;
                            break;
                        }
                    }
                }
            }

            if (!isImported) {
                System.err.println("Could not find import for extend name.");
                System.exit(42);
            }
        }

        if (implementsTable != null) {
            boolean isImported = false;
            for (int i = 0; i < implementsTable.size(); ++i) {
                final Name implementsName = implementsTable.get(i);

                if (isImportSuffix(implementsName)) {
                    isImported = true;
                    break;
                }

                for (final ClassScope classScope : classTable) {
                    if (classScope != null
                            && implementsName.getClassName().equals(classScope.name)) {
                        if (classScope.packageName != null) {
                            final Name qualifiedName = classScope.packageName.generateAppendedPackageName(implementsName.getClassName());
                            if (implementsName.checkPackageMatch(classScope.packageName)) {
                                isImported = true;
                                break;
                            } else if ((isOnDemandImportSuffix(classScope.packageName)
                                    || classScope.packageName.equals(this.packageName))
                                    &&  implementsName.containsSomePackageSuffix(classScope.packageName)) {
                                System.out.println("Changing " + i + " to: " + qualifiedName);
                                if (!classScope.packageName.isDefault()) {
                                    implementsTable.set(i, qualifiedName);
                                }
                                isImported = true;
                                break;
                            }
                        } else if (this.packageName == null) {
                            isImported = true;
                            break;
                        }
                    }
                }
            }

            if (!isImported) {
                System.err.println("Could not find import for implements name.");
                System.exit(42);
            }
        }
    }

    private boolean isImportSuffix(final Name extendsName) {
        for (final ImportScope importScope : imports) {
            if (importScope.type == ImportScope.IMPORT_TYPE.ON_DEMAND) continue;
            if (importScope.name.containsSuffixName(extendsName)) return true;
        }
        return false;
    }

    private boolean isOnDemandImportSuffix(final Name packageName) {
        for (final ImportScope importScope : imports) {
            if (importScope.type == SINGLE) continue;
            if (packageName.containsPrefixName(importScope.name)) return true;
        }
        return false;
    }

    public boolean hasSingleTypeImportOfClass(String className){
        for (ImportScope importScope : imports){
            if (importScope.type == SINGLE){
                System.out.println("TESTIMG " + importScope.name.getActualSimpleName() + " : " + className);
                if (importScope.name.getActualSimpleName().equals(className)){
                    return true;
                }
            }
        }
        return false;
    }
}
