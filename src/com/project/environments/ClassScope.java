package com.project.environments;

import com.project.environments.ast.ASTHead;
import com.project.environments.structure.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.project.environments.ImportScope.IMPORT_TYPE.SINGLE;
import static com.project.environments.structure.Name.containsPrefixName;
import static com.project.environments.structure.Name.generateFullyQualifiedName;

public class ClassScope extends Scope {

    public enum CLASS_TYPE {
        INTERFACE,
        CLASS
    }

    private final Map<String, ClassScope> singleImportMap;
    private final Map<String, ClassScope> onDemandImportMap;
    private final Map<String, ClassScope> inPackageImportMap;

    public final String name;
    public final ASTHead ast;
    public final CLASS_TYPE type;
    public final Name packageName;

    public final ArrayList<String> modifiers;
    public final ArrayList<ImportScope> imports;
    public final ArrayList<Name> usedTypeNames;

    public final ArrayList<Name> implementsTable;
    public ArrayList<Name> extendsTable;

    public final ArrayList<MethodScope> methodTable;
    public final ArrayList<ConstructorScope> constructorTable;
    private final ArrayList<FieldScope> fieldTable;

    public ClassScope(final String name, final ASTHead ast) {
        this.name = name;
        this.ast = ast;
        this.packageName = ast.getPackageName();
        this.imports = ast.getImports(this);
        this.modifiers = ast.getClassModifiers();

        singleImportMap = new HashMap<>();
        onDemandImportMap = new HashMap<>();
        inPackageImportMap = new HashMap<>();

        final ASTHead classDeclaration = ast.getClassDeclaration();

        this.type = classDeclaration.getClassType();

        implementsTable = classDeclaration.getClassInterfaces();

        if (this.type == CLASS_TYPE.INTERFACE) {
            extendsTable = classDeclaration.getInterfaceSuperInterfaces();
        } else {
            extendsTable = classDeclaration.getClassSuperClass();
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

        fieldTable = new ArrayList<>();
        generateFieldTable();
        checkDuplicateFields();

        methodTable = new ArrayList<>();
        generateMethodTable();

        constructorTable = new ArrayList<>();
        generateConstructorTable();

        this.usedTypeNames = ast.getUsedTypeNames().stream()
                .map(Name::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean isJavaLangObject() {
        return name.equals("Object") && packageName.equals(Name.generateJavaLangPackageName());
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

    private void checkDuplicateSupers() {
        if (extendsTable == null) return;

        for (int i = 0; i < extendsTable.size(); ++i) {
            for (int j = i + 1; j < extendsTable.size(); ++j) {
                if (extendsTable.get(i).equals(extendsTable.get(j))) {
                    System.err.println("Found duplicate extends in same class.");
                    System.exit(42);
                }
            }
        }
    }

    private void checkDuplicateImplements() {
        if (implementsTable == null) return;

        for (int i = 0; i < implementsTable.size(); ++i) {
            for (int j = i + 1; j < implementsTable.size(); ++j) {
                if (implementsTable.get(i).equals(implementsTable.get(j))) {
                    System.err.println("Found duplicate implements in same class.");
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

    /**
     * Generates a set of maps that link our imports to any existent classes.
     *
     * @param classTable Required to know about the classes.
     */
    public void generateImportMaps(final ArrayList<ClassScope> classTable) {
        for (final ImportScope importScope : imports) {
            if (importScope.type == SINGLE) {
                final String simpleName = importScope.getSimpleName();
                final Name packageName = importScope.getPackageName();

                // Check that the import isn't the same as our name.
                // Check no import clashes with class or interface definitions
                if (importScope.name.getClassName().equals(this.name)
                        && !importScope.name.checkPackageMatch(this.packageName)) {
                    System.err.println("Found import with same name as class.");
                    System.exit(42);
                }

                // Look for a class that matches the simple and package name.
                boolean foundClass = false;
                for (final ClassScope classScope : classTable) {
                    if (classScope.name.equals(simpleName)
                            && classScope.packageName.equals(packageName)) {

                        // If the map already has the class, we may have a duplicate!
                        // Check if it's the exact same class; that's fine.
                        if (singleImportMap.containsKey(simpleName)
                                && !singleImportMap.get(simpleName).packageName.equals(packageName)) {
                            System.err.println("Found duplicate single-type imports.");
                            System.exit(42);
                        }

                        singleImportMap.put(simpleName, classScope);
                        foundClass = true;
                        break;
                    }
                }

                // If we can't find a class that matches the import, something's wrong.
                if (!foundClass) {
                    System.err.println("Couldn't link single-type import to class.");
                    System.exit(42);
                }
            } else {
                final Name packageName = importScope.getPackageName();

                // Look for any classes that match the package name.
                boolean foundClass = false;
                for (final ClassScope classScope : classTable) {
                    if (containsPrefixName(classScope.packageName, packageName)) {
                        final String simpleName = classScope.name;

                        // We can have duplicates in our on-demand imports, but we can't use them.
                        // We'll put the import to a null if we find it, so we error out if we use it.
                        // Again, we make sure it's not exactly the same class.
                        if (onDemandImportMap.containsKey(simpleName)
                                && !onDemandImportMap.get(simpleName).packageName.equals(packageName)) {
                            onDemandImportMap.replace(simpleName, null);
                        } else {
                            onDemandImportMap.put(simpleName, classScope);
                        }

                        foundClass = true;
                    }
                }

                // If we can't find any class that matches the import, something's wrong.
                if (!foundClass) {
                    System.err.println("Couldn't link on-demand type import to any class.");
                    System.exit(42);
                }
            }
        }

        // Lastly, get all the classes in our own package.
        for (final ClassScope classScope : classTable) {
            if (classScope.packageName.equals(this.packageName)) {
                inPackageImportMap.put(classScope.name, classScope);
            }
        }
    }

    public void linkTypes() {
        linkSuperTypes();
        linkImplementsTypes();
        linkFieldsTypes();

        linkConstructorTypes();
        linkMethodParameters();
    }

    private void linkConstructorTypes() {
        if (constructorTable == null) {
            System.err.println("Found no constructor for class " + name + ".");
            System.exit(42);
        }

        for (final ConstructorScope constructorScope : constructorTable) {
            constructorScope.linkTypes();
        }
    }


    private void linkSuperTypes() {
        if (extendsTable == null) return;

        for (int i = 0; i < extendsTable.size(); ++i) {
            final Name superName = extendsTable.get(i);

            // If the name has a package, it's already qualified.
            if (superName.getPackageName() == null) {
                extendsTable.set(i, findImportedType(superName.getSimpleName()));
            }
        }
    }

    private void linkImplementsTypes() {
        if (implementsTable == null) return;

        for (int i = 0; i < implementsTable.size(); ++i) {
            final Name superName = implementsTable.get(i);

            // If the name has a package, it's already qualified.
            if (superName.getPackageName() == null) {
                implementsTable.set(i, findImportedType(superName.getSimpleName()));
            }
        }
    }

    public void duplicateCheck() {
        checkDuplicateImplements();
        checkDuplicateSupers();
    }

    public Name findImportedType(final String simpleName) {
        final Name name = getImportedType(simpleName);

        if (name == null) {
            // If we can't find an import, we have a missing type.
            System.err.println("Couldn't link " + simpleName + " to any imported type.");
            System.exit(42);
        }

        // Check if a prefix of our name itself resolves to a type.
        for (Name prefix = name.getPackageName(); prefix != null; prefix = prefix.getPackageName()) {
            if (getImportedType(prefix.getSimpleName()) != null) {
                System.err.println("Prefix of type was itself type.");
                System.exit(42);
            }
        }

        return name;
    }

    private Name getImportedType(final String simpleName) {
        // First, check if it's already in our package.
        final Name inPackageImportName = findInPackageImport(simpleName);
        if (inPackageImportName != null) return inPackageImportName;

        // Look for the class in the single import table first; it has precedent.
        final Name singleImportName = findSingleImport(simpleName);
        if (singleImportName != null) return singleImportName;

        // Lastly, look in the on demand import table.
        return findOnDemandImport(simpleName);
    }

    private Name findSingleImport(final String simpleName) {
        final ClassScope superClass = singleImportMap.get(simpleName);

        // Null if we can't find the class in the single import table.
        if (superClass != null) {
            return generateFullyQualifiedName(superClass.name, superClass.packageName);
        } else return null;
    }

    private Name findOnDemandImport(final String simpleName) {
        final ClassScope superClass = onDemandImportMap.get(simpleName);

        // Null if we can't find the class in the on-demand import table.
        if (superClass != null) {
            return generateFullyQualifiedName(superClass.name, superClass.packageName);
        } else return null;
    }

    private Name findInPackageImport(final String simpleName) {
        final ClassScope superClass = inPackageImportMap.get(simpleName);

        // Null if we can't find the class in the in-package import table.
        if (superClass != null) {
            return generateFullyQualifiedName(superClass.name, superClass.packageName);
        } else return null;
    }

    private void linkMethodParameters() {
        if (methodTable == null) return;

        for (final MethodScope methodScope : methodTable) {
            methodScope.linkParameters();
        }
    }

    private void linkFieldsTypes() {
        if (fieldTable == null) return;

        for (final FieldScope fieldScope : fieldTable) {
            fieldScope.linkTypes();
        }
    }
}
