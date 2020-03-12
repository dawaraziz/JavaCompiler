package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;
import com.project.environments.expressions.Expression;
import com.project.environments.structure.Name;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;
import com.project.util.Triplet;
import resources.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.project.environments.scopes.ImportScope.IMPORT_TYPE.SINGLE;
import static com.project.environments.structure.Name.containsPrefixName;
import static com.project.environments.structure.Name.generateFullyQualifiedName;
import static com.project.scanner.structure.Kind.TYPENAME;

public class ClassScope extends Scope {

    private static final ClassScope duplicateHolderScope = new ClassScope();

    public enum CLASS_TYPE {
        INTERFACE,
        CLASS
    }

    private final Map<String, ClassScope> singleImportMap;
    private final Map<String, ClassScope> onDemandImportMap;
    private final Map<String, ClassScope> inPackageImportMap;

    public final ArrayList<ClassScope> classTable;
    public HashMap<String, ClassScope> classMap;

    public final Map<String, PackageScope> packageMap;

    public final ASTHead ast;
    public final CLASS_TYPE classType;
    public final Name packageName;

    public final ArrayList<String> modifiers;
    public final ArrayList<ImportScope> imports;
    public final ArrayList<Name> usedTypeNames;

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
        this.type = new Type(name, packageName);
        this.packageMap = new HashMap<>();

        singleImportMap = new HashMap<>();
        onDemandImportMap = new HashMap<>();
        inPackageImportMap = new HashMap<>();
        classTable = new ArrayList<>();

        final ASTHead classDeclaration = ast.getClassDeclaration();

        this.classType = classDeclaration.getClassType();

        implementsTable = classDeclaration.getClassInterfaces();

        if (this.classType == CLASS_TYPE.INTERFACE) {
            extendsTable = classDeclaration.getInterfaceSuperInterfaces();
        } else {
            extendsTable = classDeclaration.getClassSuperClass();
        }

        if (this.classType == CLASS_TYPE.CLASS) {
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

    public ClassScope() {
        singleImportMap = null;
        onDemandImportMap = null;
        inPackageImportMap = null;
        packageMap = null;
        classTable = null;

        ast = null;
        classType = null;
        packageName = null;

        modifiers = null;
        imports = null;
        usedTypeNames = null;

        implementsTable = null;
        extendsTable = null;

        methodTable = null;
        constructorTable = null;
        fieldTable = null;
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

    public void setClassMap(final HashMap<String, ClassScope> classMap) {
        this.classMap = classMap;
    }

    private Boolean containsMethod(final MethodScope methodScope) {
        for (final MethodScope method : methodTable) {
            final boolean signatureMatch = method.equals(methodScope);
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
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    /**
     * Generates a set of maps that link our imports to any existent classes.
     *
     * @param classTable Required to know about the classes.
     */
    public void generateImportMaps(final ArrayList<ClassScope> classTable) {
        for (final ImportScope importScope : imports) {
            if (importScope.importType == SINGLE) {
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
                        // We'll put the import to a special scope if we find it, so we error out if we use it.
                        // Again, we make sure it's not exactly the same class.
                        if (onDemandImportMap.containsKey(simpleName)
                                && !onDemandImportMap.get(simpleName).packageName.equals(packageName)) {
                            onDemandImportMap.replace(simpleName, duplicateHolderScope);
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

        this.classTable.addAll(classTable);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        linkConstructorTypes();
        linkMethodParameters();
    }

    public ArrayList<MethodScope> getAllMethods() {
        final ArrayList<MethodScope> inheritedMethods = new ArrayList<>();
        inheritedMethods.addAll(this.methodTable);

        final Stack<ClassScope> classes = new Stack<>();
        classes.push(this);

        while (!classes.isEmpty()) {
            final ClassScope curClass = classes.pop();
            inheritedMethods.addAll(getMethods(curClass.extendsTable, classes));
        }

        return inheritedMethods;
    }

    private ArrayList<MethodScope> getMethods(final ArrayList<Name> superList,
                                              final Stack<ClassScope> classes) {
        final ArrayList<MethodScope> methods = new ArrayList<>();

        if (superList == null) return methods;

        for (final Name superName : superList) {
            final ClassScope superClass = classMap.get(superName.getDefaultlessQualifiedName());

            if (superClass == null) continue;

            classes.push(superClass);

            if (superClass.methodTable != null) {
                methods.addAll(superClass.methodTable);
            }
        }

        return methods;
    }

    private void linkConstructorTypes() {
        if (constructorTable == null) {
            System.err.println("Found no constructor for class " + name + ".");
            System.exit(42);
        }

        for (final ConstructorScope constructorScope : constructorTable) {
            constructorScope.linkTypesToQualifiedNames(this);
        }
    }


    public void linkSuperTypes() {
        if (extendsTable == null) return;

        for (int i = 0; i < extendsTable.size(); ++i) {
            final Name superName = extendsTable.get(i);

            // If the name has a package, it's already qualified.
            if (superName.getPackageName() == null) {
                extendsTable.set(i, findImportedType(superName.getSimpleName()));
            }
        }
    }

    public void linkMethodTypes() {
        if (this.methodTable != null) {
            methodTable.forEach(c -> c.linkTypes(this));
        }
    }

    public void linkImplementsTypes() {
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
            final Name possibleConflict = getImportedType(prefix.getSimpleName());

            if (possibleConflict != null && possibleConflict.getPackageName() == prefix.getPackageName()) {
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
        if (methodTable != null) methodTable.forEach(c -> c.linkTypesToQualifiedNames(this));
    }

    public void linkFieldsTypes() {
        if (fieldTable != null) fieldTable.forEach(c -> c.linkTypesToQualifiedNames(this));
    }

    @Override
    public void checkTypeSoundness() {
        for (final FieldScope fieldScope : fieldTable) {
            fieldScope.checkTypeSoundness();
        }
        for (final MethodScope methodScope : methodTable) {
            methodScope.checkTypeSoundness();
        }

        //Dealing with assignable
        checkAssignments(ast);


    }

    public void checkAssignments(ASTHead astHead){
        ASTNode ast = astHead.unsafeGetHeadNode();
        ArrayList<ASTNode> declarations = ast.findNodesWithLexeme("LOCALVARIABLEDECLARATION", "FIELDDECLARATION");
        System.out.println("CALUM: " + declarations.size() + " : " +  name);

        // for each local variable declaration get the LHS type and RHS
        for (ASTNode node : declarations){
            // Boolean whether type array and its kind
            Triplet<Boolean, Kind, String> lhs_type = getDeclarationLHSType(node);
            Triplet<Boolean, Kind, String> rhs_type = getDeclarationRHSType(node);
            System.out.println("Class: " + name);
            System.out.println("GOT LHS: " + lhs_type);
            System.out.println("GOT RHS: " + rhs_type);
            System.out.println("EQUAL: " + lhs_type.equals(rhs_type));

            // If rhs is null it is a declaration not an assignment we can break
            if (rhs_type.getO2() == Kind.NULL || rhs_type.getO2() == null){
                System.out.println("Breaking: " + rhs_type.getO2() + rhs_type.getO2() == null);
                break;
            }

            // False if both arn't arrays and its not just a declaration
            if ((rhs_type.getO2() != Kind.NULL) && (lhs_type.getO1() != rhs_type.getO1())){
                // if they are both a typeName however this is a special case -- deal with it later
                if (lhs_type.getO2() != TYPENAME || rhs_type.getO2() != TYPENAME) {
                    System.err.println("LHS or RHS is an array and the other is not in: " + name);
                    System.exit(42);
                }
            }

            // If both are a numberic type and neither are arrays
            if (bothNumbericType(lhs_type, rhs_type) && !lhs_type.getO1() && !rhs_type.getO1()) {
                //Check if the numeric type fits in the other
                if (!legalNumericCast(lhs_type, rhs_type)) {
                    System.err.println("LHS type of Declaration does not match RHS in: " + name);
                    System.exit(42);
                }
            }

            // If both are typename than check the rhs is as or extends the lhs
            else if(rhs_type.getO2() == TYPENAME && lhs_type.getO2() == TYPENAME){
                // TODO: ensure extension or same -- STILL NEED TO ADD EXTENSION CODE THIS IS WHY LEGALS FAIL
                if (!lhs_type.getO3().equals(rhs_type.getO3())){
                    System.err.println("LHS of type " + lhs_type.getO3() + " is not the same as or doesn't extend RHS of : " + rhs_type.getO3() + " IN : " + name);
                    System.exit(42);
                }

                // if one is an array and one isn't fail unless it is the lhs that isn't
                if (lhs_type.getO1() != rhs_type.getO1()){
                    if (lhs_type.getO1() == true){
                        System.err.println("LHS is array of " + lhs_type.getO3() + " and RHS is not : " + name);
                        System.exit(42);
                    }
                }
            }

            else {
                // If RHS is null it is legal, if not make sure the types are equal (i.e both array or not, and type)
                if (rhs_type.getO2() != Kind.NULL && !lhs_type.equals(rhs_type)) {
                    System.err.println("LHS type of Declaration does not match RHS in: " + name);
                    System.exit(42);
                }
            }
        }
    }

    public boolean bothNumbericType(Triplet<Boolean, Kind, String> lhs, Triplet<Boolean, Kind, String> rhs){
        ArrayList<Kind> numTypes = new ArrayList<>();
        numTypes.add(Kind.BYTE);
        numTypes.add(Kind.SHORT);
        numTypes.add(Kind.INT);
        if (numTypes.contains(lhs.getO2()) && numTypes.contains(rhs.getO2())){
            return true;
        }
        return false;
    }

    public boolean legalNumericCast(Triplet<Boolean, Kind, String> lhs, Triplet<Boolean, Kind, String> rhs) {
        ArrayList<Kind> numTypes = new ArrayList<>();
        numTypes.add(Kind.BYTE);
        numTypes.add(Kind.SHORT);
        numTypes.add(Kind.INT);
        if (numTypes.indexOf(lhs.getO2()) >= numTypes.indexOf(rhs.getO2())){
            return true;
        }
        return false;
    }

    // Takes a LOCALVARIABLEDECLARATION or FIELDDECLARATION node as input
    public Triplet<Boolean, Kind, String> getDeclarationLHSType(ASTNode node){
        ASTNode typeNode = null;
        if (node.lexeme.equals("LOCALVARIABLEDECLARATION")) {
            System.out.println("Local Variable Declaration");
            typeNode = node.children.get(node.children.size() - 1);
        }
        else if (node.lexeme.equals("FIELDDECLARATION")) {
            // assumes a field Declaration always has a modifier
            System.out.println("Field Declaration");
            typeNode = node.children.get(node.children.size()-2);
        }
        System.out.println("HERE --------------------");
        node.printAST();

        // An array type
        if (typeNode.lexeme.equals("ARRAYTYPE")){
            ASTNode nextNode =  typeNode.children.get(typeNode.children.size()-1);
            // if array of integral type need to go one more level
            if (nextNode.kind != TYPENAME){
                Kind type = nextNode.children.get(nextNode.children.size()-1).kind;
                System.out.println("TYPE : !! " + type);
                return new Triplet(true, type, "");
            }
            else{
                return new Triplet(true, TYPENAME, nextNode.lexeme);
            }
        }
        // some object type
        else if (typeNode.kind == TYPENAME){
            if (typeNode.lexeme.equals("String")){
                return new Triplet(false, Kind.STRING_LITERAL, "");
            }
            return new Triplet(false, TYPENAME, typeNode.lexeme);
        }
        // Some primitive type
        else {
            Kind type = typeNode.children.get(typeNode.children.size()-1).kind;
            return new Triplet<>(false, type, "");
        }
    }

    public Kind translateType(Kind type){
        if (type == Kind.INTEGER_LITERAL){
            return Kind.INT;
        }
        if (type == Kind.FALSE || type == Kind.TRUE){
            return Kind.BOOLEAN;
        }
        if (type == Kind.CHARACTER_LITERAL){
            return Kind.CHAR;
        }
        return type;
    }

    // Takes a LOCALVARIABLEDECLARATION or FIELDDECLARATION node as input
    public Triplet<Boolean, Kind, String> getDeclarationRHSType(ASTNode node){
        // get variabledeclarator child
        ASTNode varDecNode = node.findFirstDirectChildNodeWithLexeme("VARIABLEDECLARATOR");

        if (varDecNode != null) {
            // Figure out what after the = sign resolves to
            ASTNode nodeAfterEquals = varDecNode.findFirstChildAfterChildWithKind(Kind.EQUAL);
            System.out.println("Got: " + nodeAfterEquals);

            // If a class instance
            if (nodeAfterEquals.lexeme.equals("CLASSINSTANCECREATIONEXPRESSION")){
                String objType = nodeAfterEquals.children.get(nodeAfterEquals.children.size()-2).lexeme;
                return new Triplet<>(false, TYPENAME, objType);
            }

            // If a Literal Instance
            if (nodeAfterEquals.lexeme.equals("LITERAL")){
                Kind type = nodeAfterEquals.children.get(nodeAfterEquals.children.size()-1).kind;
                type = translateType(type);
                return new Triplet<>(false, type, "");
            }

            // If array creation expression
            if (nodeAfterEquals.lexeme.equals("ARRAYCREATIONEXPRESSION")){
                ASTNode innerTypeNode = nodeAfterEquals.children.get(nodeAfterEquals.children.size()-2);
                if (innerTypeNode.kind != TYPENAME){
                    Kind type = innerTypeNode.children.get(innerTypeNode.children.size()-1).kind;
                    type = translateType(type);
                    return new Triplet(true, type, "");
                }
                else{
                    return new Triplet(true, TYPENAME, innerTypeNode.lexeme);
                }
            }

            // Need to add multiplicative expression etc.
            if (nodeAfterEquals.lexeme.equals("CASTEXPRESSION")) {
                ASTNode innerTypeNode = nodeAfterEquals.children.get(nodeAfterEquals.children.size()-2);
                boolean isArr = false;
                if (nodeAfterEquals.children.get(nodeAfterEquals.children.size()-3).lexeme.equals("DIMS")){
                    isArr = true;
                }
                if (innerTypeNode.kind != TYPENAME){
                    Kind type = innerTypeNode.children.get(innerTypeNode.children.size()-1).kind;
                    type = translateType(type);
                    return new Triplet(isArr, type, "");
                }
                else{
                    return new Triplet(isArr, TYPENAME, innerTypeNode.lexeme);
                }
            }

        }
        return new Triplet<>(false, null, "");
    }

    public boolean checkIdentifier(final String identifier) {
        return this.name.equals(identifier);
    }

    public boolean checkIdentifierAgainstSingleImports(final String identifier) {
        return singleImportMap.get(identifier) != null;
    }

    public boolean checkIdentifierAgainstPackageImports(final String identifier) {
        return inPackageImportMap.get(identifier) != null;
    }

    public boolean checkIdentifierAgainstOnDemandImports(final String identifier) {
        final ClassScope scope = onDemandImportMap.get(identifier);

        // We need to do this to identify duplicates. If we previously IDed a
        // duplicate, the map has a special scope in it.
        if (scope == duplicateHolderScope) {
            System.err.println("Requested on-demand import with non-singular resolution.");
            System.exit(42);
        }

        return onDemandImportMap.get(identifier) != null;
    }

    public boolean checkIdentifierAgainstFields(final String identifier) {
        for (final FieldScope fieldScope : fieldTable) {
            if (fieldScope.checkIdentifier(identifier)) return true;
        }
        return false;
    }

    public boolean checkIdentifierAgainstMethods(final String identifier) {
        for (final MethodScope methodScope : methodTable) {
            if (methodScope.checkIdentifier(identifier)) return true;
        }
        return false;
    }

    public ClassScope findClass(final String fullName) {
        for (final ClassScope classScope : classTable) {
            if (Name.generateFullyQualifiedName(classScope.name, classScope.packageName)
                    .getQualifiedName().equals(fullName)) {
                return classScope;
            }
        }
        return null;
    }

    public Scope resolveSimpleTypeName(final String identifier) {
        if (checkIdentifier(identifier)) {
            return this;
        } else if (checkIdentifierAgainstSingleImports(identifier)) {
            return singleImportMap.get(identifier);
        } else if (checkIdentifierAgainstPackageImports(identifier)) {
            return inPackageImportMap.get(identifier);
        } else if (checkIdentifierAgainstOnDemandImports(identifier)) {
            return onDemandImportMap.get(identifier);
        } else {
            System.err.println("Could not resolve type name.");
            System.exit(42);
            return null;
        }
    }

    public FieldScope getIdentifierFromFields(final String identifier) {
        for (final FieldScope fieldScope : fieldTable) {
            if (fieldScope.checkIdentifier(identifier)) return fieldScope;
        }

        if (extendsTable == null) return null;

        for (final Name className : extendsTable) {
            final FieldScope fieldScope = getClassFromPackage(className.getPackageString(),
                    className.getSimpleName()).getIdentifierFromFields(identifier);
            if (fieldScope != null) return fieldScope;
        }

        return null;
    }

    public boolean isNamePrefixOfPackage(final String prefix) {
        for (final String packageName : packageMap.keySet()) {
            if (packageName.startsWith(prefix)) return true;
        }
        return false;
    }

    public ClassScope getClassFromPackage(final String packageName, final String simpleName) {
        final PackageScope packageScope = packageMap.get(packageName);

        if (packageScope == null) return null;

        return packageScope.getClass(simpleName);
    }

    public MethodScope getMethodWithIdentifierAndParameters(final String identifier,
                                                            final ArrayList<Expression> parameters) {

        // First, we inspect ourselves.
        for (final MethodScope method : getAllMethods()) {
            if (method.name.equals(identifier)) {

                if (parameters.size() == 0 && method.parameters == null) {
                    return method;
                } else if (method.parameters == null) {
                    continue;
                }

                // Check if the parameters match up.
                if (parameters.size() != method.parameters.size()) continue;
                boolean parametersMatch = true;
                for (int i = 0; i < parameters.size(); ++i) {
                    if (!(parameters.get(i).type.equals(method.parameters.get(i).type))) {
                        parametersMatch = false;
                        break;
                    }
                }

                if (parametersMatch) return method;
            }
        }

        return null;
    }

    public Type generateType() {
        return new Type(name, packageName);
    }

    public boolean isSubClassOf(final ClassScope LHSClass) {
        if (LHSClass.equals(this)) return true;

        if (extendsTable == null) return false;

        for (final Name className : extendsTable) {
            final ClassScope superClass = getClassFromPackage(className.getPackageString(),
                    className.getSimpleName());

            if (superClass.isSubClassOf(LHSClass)) return true;
        }

        return false;
    }
}
