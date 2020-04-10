package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;
import com.project.environments.expressions.ArgumentListExpression;
import com.project.environments.expressions.Expression;
import com.project.environments.expressions.NameExpression;
import com.project.environments.structure.Name;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;
import com.project.util.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.project.Main.classTable;
import static com.project.Main.objectClass;
import static com.project.Main.packageMap;
import static com.project.Main.staticExternSet;
import static com.project.Main.writeCodeToFile;
import static com.project.environments.scopes.ClassScope.CLASS_TYPE.CLASS;
import static com.project.environments.scopes.ClassScope.CLASS_TYPE.INTERFACE;
import static com.project.environments.scopes.ImportScope.IMPORT_TYPE.SINGLE;
import static com.project.environments.scopes.MethodScope.generateEpilogueCode;
import static com.project.environments.scopes.MethodScope.generatePrologueCode;
import static com.project.environments.structure.Name.containsPrefixName;
import static com.project.environments.structure.Name.generateFullyQualifiedName;
import static com.project.scanner.structure.Kind.TYPENAME;

public class ClassScope extends Scope {

    public final LinkedHashSet<String> methodExternList = new LinkedHashSet<>();

    private static final ClassScope duplicateHolderScope = new ClassScope();

    public String generateSITGlobalLabel() {
        return "global " + callSITLabel();
    }

    public String generateSITExternLabel() {
        return "extern " + callSITLabel();
    }

    public String generateSubtypeGlobalLabel() {
        return "global " + callSubtypeTableLabel();
    }

    public String generateSubtypeExternLabel() {
        return "extern " + callSubtypeTableLabel();
    }

    public int getWordSize() {
        return codeFieldOrder.size() + 1;
    }

    public ArrayList<String> generateFieldInitializationCode(final int thisOffset) {
        final ArrayList<String> code = new ArrayList<>();
        for (final FieldScope fieldScope : codeFieldOrder) {
            if (fieldScope.initializer != null) {
                code.addAll(fieldScope.initializer.generatei386Code());
                code.add("push eax");
                code.add("mov eax, [ebp + " + thisOffset + "] ; Get this object.");
                code.add("mov eax, eax + " + getNonStaticFieldOffset(fieldScope) + "; Find the field location");
                code.add("pop word [eax] ; Put the value of the field into the object.");
                code.add("");
            }
        }
        return code;
    }

    public enum CLASS_TYPE {
        INTERFACE,
        CLASS
    }

    private final Map<String, ClassScope> singleImportMap;
    private final Map<String, ClassScope> onDemandImportMap;
    private final Map<String, ClassScope> inPackageImportMap;

    public HashMap<String, ClassScope> classMap;

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

    public LinkedHashSet<MethodScope> codeMethodOrder = new LinkedHashSet<>();
    public LinkedHashSet<FieldScope> codeFieldOrder = new LinkedHashSet<>();

    public ClassScope(final String name, final ASTHead ast) {
        this.name = name;
        this.ast = ast;
        this.packageName = ast.getPackageName();
        this.imports = ast.getImports(this);
        this.modifiers = ast.getClassModifiers();
        this.type = new Type(name, packageName);

        singleImportMap = new HashMap<>();
        onDemandImportMap = new HashMap<>();
        inPackageImportMap = new HashMap<>();

        final ASTHead classDeclaration = ast.getClassDeclaration();

        this.classType = classDeclaration.getClassType();

        implementsTable = classDeclaration.getClassInterfaces();

        if (this.classType == INTERFACE) {
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

    public void generateObjectMethods() {
        if (extendsTable != null && extendsTable.size() > 0) return;

        for (final MethodScope objectMethod : objectClass.methodTable) {
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
     */
    public void generateImportMaps() {
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

    public void linkConstructorType() {
        if (this.constructorTable != null) {
            constructorTable.forEach(c -> c.linkTypes(this));
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
        System.out.println("CALUM CHECKING CONDITIONALS --------------- " + this.name);
        methodTable.forEach(MethodScope::checkConditionals);
        constructorTable.forEach(ConstructorScope::checkConditionals);
        System.out.println("CALUM CHECKING RETURNS --------------- " + this.name);
        methodTable.forEach(n -> n.checkReturnedTypes(classMap));
        constructorTable.forEach(n -> n.checkReturnedTypes(classMap));
//        constructorTable.forEach();
//        fieldTable.forEach();


    }

    public void checkAssignments(ASTHead astHead) {
        ASTNode ast = astHead.unsafeGetHeadNode();
        ArrayList<ASTNode> declarations = ast.findNodesWithLexeme("LOCALVARIABLEDECLARATION", "FIELDDECLARATION");
        System.out.println("CALUM: " + declarations.size() + " : " + name);

        // for each local variable declaration get the LHS type and RHS
        for (ASTNode node : declarations) {
            // Boolean whether type array and its kind
            Triplet<Boolean, Kind, String> lhs_type = getDeclarationLHSType(node);
            Triplet<Boolean, Kind, String> rhs_type = getDeclarationRHSType(node);
            System.out.println("Class: " + name);
            System.out.println("GOT LHS: " + lhs_type);
            System.out.println("GOT RHS: " + rhs_type);
            System.out.println("EQUAL: " + lhs_type.equals(rhs_type));

            // If rhs is null it is a declaration not an assignment we can break
            if (rhs_type.getO2() == null) {
                System.out.println("Breaking: " + rhs_type.getO2() + rhs_type.getO2() == null);
                break;
            }

            //TODO: What can't i declare null?
            // if rhs declared null
            if (rhs_type.getO2() == Kind.NULL) {
                if (lhs_type.getO2() == Kind.INT && !lhs_type.getO1()) {
                    System.err.println("Can't assign null to integer");
                    System.exit(42);
                }
                break;
            }

            // False if both arn't arrays and its not just a declaration
            if ((rhs_type.getO2() != Kind.NULL) && (lhs_type.getO1() != rhs_type.getO1())) {
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
            else if (rhs_type.getO2() == TYPENAME && lhs_type.getO2() == TYPENAME) {
                // TODO: ensure extension or same -- STILL NEED TO ADD EXTENSION CODE THIS IS WHY LEGALS FAIL
                ClassScope lhsScope = (ClassScope) resolveSimpleTypeName(lhs_type.getO3());
                ClassScope rhsScope = (ClassScope) resolveSimpleTypeName(rhs_type.getO3());

                if (!rhsScope.isSubClassOf(lhsScope)) {
                    System.err.println("RHS of type " + rhs_type.getO3() + " is not the same as or doesn't extend LHS of : " + lhs_type.getO3() + " IN : " + name);
                    System.exit(42);
                }

                // if one is an array and one isn't fail unless it is the lhs that isn't
                if (lhs_type.getO1() != rhs_type.getO1()) {
                    if (lhs_type.getO1() == true) {
                        System.err.println("LHS is array of " + lhs_type.getO3() + " and RHS is not : " + name);
                        System.exit(42);
                    }
                }
            } else {
                // If RHS is null it is legal, if not make sure the types are equal (i.e both array or not, and type)
                if (!lhs_type.equals(rhs_type)) {
                    // Don't fail in the special case the LHS is a string object and RHS is string literal
                    if (!(lhs_type.getO3().equals("String") && rhs_type.getO2() == Kind.STRING_LITERAL)) {
                        System.err.println("LHS type of Declaration does not match RHS in: " + name);
                        System.exit(42);
                    }
                }
            }
        }
    }

    public boolean bothNumbericType(Triplet<Boolean, Kind, String> lhs, Triplet<Boolean, Kind, String> rhs) {
        ArrayList<Kind> numTypes = new ArrayList<>();
        numTypes.add(Kind.BYTE);
        numTypes.add(Kind.SHORT);
        numTypes.add(Kind.INT);
        if (numTypes.contains(lhs.getO2()) && numTypes.contains(rhs.getO2())) {
            return true;
        }
        return false;
    }

    public boolean legalNumericCast(Triplet<Boolean, Kind, String> lhs, Triplet<Boolean, Kind, String> rhs) {
        ArrayList<Kind> numTypes = new ArrayList<>();
        numTypes.add(Kind.BYTE);
        numTypes.add(Kind.SHORT);
        numTypes.add(Kind.INT);
        if (numTypes.indexOf(lhs.getO2()) >= numTypes.indexOf(rhs.getO2())) {
            return true;
        }
        return false;
    }

    // Takes a LOCALVARIABLEDECLARATION or FIELDDECLARATION node as input
    public Triplet<Boolean, Kind, String> getDeclarationLHSType(ASTNode node) {
        ASTNode typeNode = null;
        if (node.lexeme.equals("LOCALVARIABLEDECLARATION")) {
            System.out.println("Local Variable Declaration");
            typeNode = node.children.get(node.children.size() - 1);
        } else if (node.lexeme.equals("FIELDDECLARATION")) {
            // assumes a field Declaration always has a modifier
            System.out.println("Field Declaration");
            typeNode = node.children.get(node.children.size() - 2);
        }
        System.out.println("HERE --------------------");
        node.printAST();

        // An array type
        if (typeNode.lexeme.equals("ARRAYTYPE")) {
            ASTNode nextNode = typeNode.children.get(typeNode.children.size() - 1);
            // if array of integral type need to go one more level
            if (nextNode.kind != TYPENAME) {
                Kind type = nextNode.children.get(nextNode.children.size() - 1).kind;
                System.out.println("TYPE : !! " + type);
                return new Triplet(true, type, "");
            } else {
                return new Triplet(true, TYPENAME, nextNode.lexeme);
            }
        }
        // some object type
        else if (typeNode.kind == TYPENAME) {
            return new Triplet(false, TYPENAME, typeNode.lexeme);
        }
        // Some primitive type
        else {
            Kind type = typeNode.children.get(typeNode.children.size() - 1).kind;
            return new Triplet<>(false, type, "");
        }
    }

    public Kind translateType(Kind type) {
        if (type == Kind.INTEGER_LITERAL) {
            return Kind.INT;
        }
        if (type == Kind.FALSE || type == Kind.TRUE) {
            return Kind.BOOLEAN;
        }
        if (type == Kind.CHARACTER_LITERAL) {
            return Kind.CHAR;
        }
        return type;
    }

    public String getConstructorLabel(Expression arguments) {
        String label = "";


        for (ConstructorScope constructorScope: constructorTable) {

            if ((arguments == null) && (constructorScope.parameters == null)) return constructorScope.callLabel();

            else if (arguments == null) continue;

            else if (arguments instanceof NameExpression) {
                ArrayList<Expression> args = new ArrayList<>();
                args.add(arguments);
                if ((constructorScope.parameters.size() == 1) && (paramsEqual(args, constructorScope.parameters)))
                    return constructorScope.callLabel();
            }
            else if ((arguments instanceof ArgumentListExpression)
                    && (((ArgumentListExpression) arguments).arguments.size() == constructorScope.parameters.size())
                    && (paramsEqual(((ArgumentListExpression) arguments).arguments, constructorScope.parameters))) {
                return constructorScope.callLabel();
            }

        }


        return label;
    }


    private boolean paramsEqual(ArrayList<Expression> arguments, ArrayList<Parameter> params) {

        for (Expression argument : arguments) {
            boolean found = false;
            NameExpression arg = (NameExpression) argument;
            for (Parameter param : params) {
                if (param.equals(new Parameter(arg.type, arg.getNameLexeme()))) {
                    found = true;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }



    // Takes a LOCALVARIABLEDECLARATION or FIELDDECLARATION node as input
    public Triplet<Boolean, Kind, String> getDeclarationRHSType(ASTNode node) {
        // get variabledeclarator child
        ASTNode varDecNode = node.findFirstDirectChildNodeWithLexeme("VARIABLEDECLARATOR");

        if (varDecNode != null) {
            // Figure out what after the = sign resolves to
            ASTNode nodeAfterEquals = varDecNode.findFirstChildAfterChildWithKind(Kind.EQUAL);
            System.out.println("Got: " + nodeAfterEquals);

            // If a class instance
            if (nodeAfterEquals.lexeme.equals("CLASSINSTANCECREATIONEXPRESSION")) {
                String objType = nodeAfterEquals.children.get(nodeAfterEquals.children.size() - 2).lexeme;
                return new Triplet<>(false, TYPENAME, objType);
            }

            // If a Literal Instance
            if (nodeAfterEquals.lexeme.equals("LITERAL")) {
                Kind type = nodeAfterEquals.children.get(nodeAfterEquals.children.size() - 1).kind;
                type = translateType(type);
                return new Triplet<>(false, type, "");
            }

            // If array creation expression
            if (nodeAfterEquals.lexeme.equals("ARRAYCREATIONEXPRESSION")) {
                ASTNode innerTypeNode = nodeAfterEquals.children.get(nodeAfterEquals.children.size() - 2);
                if (innerTypeNode.kind != TYPENAME) {
                    Kind type = innerTypeNode.children.get(innerTypeNode.children.size() - 1).kind;
                    type = translateType(type);
                    return new Triplet(true, type, "");
                } else {
                    return new Triplet(true, TYPENAME, innerTypeNode.lexeme);
                }
            }

            // Need to add multiplicative expression etc.
            if (nodeAfterEquals.lexeme.equals("CASTEXPRESSION")) {
                ASTNode innerTypeNode = nodeAfterEquals.children.get(nodeAfterEquals.children.size() - 2);
                boolean isArr = false;
                if (nodeAfterEquals.children.get(nodeAfterEquals.children.size() - 3).lexeme.equals("DIMS")) {
                    isArr = true;
                }
                if (innerTypeNode.kind != TYPENAME) {
                    Kind type = innerTypeNode.children.get(innerTypeNode.children.size() - 1).kind;
                    type = translateType(type);
                    return new Triplet(isArr, type, "");
                } else {
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
        System.out.println("Using: " + identifier);
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

    public void assignReachability() {
        methodTable.forEach(MethodScope::assignReachability);
        constructorTable.forEach(ConstructorScope::assignReachability);
    }

    public void checkReachability() {
        methodTable.forEach(MethodScope::checkReachability);
        constructorTable.forEach(ConstructorScope::checkReachability);
    }

    public ArrayList<FieldScope> getStaticFields() {
        return fieldTable.stream()
                .filter(e -> e.modifiers.contains("static"))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void generateFieldOrder() {
        if (classType != CLASS) return;
        if (codeFieldOrder.size() != 0) return;

        final ArrayList<FieldScope> nonStaticFields = fieldTable.stream()
                .filter(e -> !e.modifiers.contains("static"))
                .collect(Collectors.toCollection(ArrayList::new));

        if (extendsTable == null || extendsTable.size() == 0) {
            codeFieldOrder.addAll(nonStaticFields);
        } else if (extendsTable.size() == 1) {
            final Name superName = extendsTable.get(0);

            final ClassScope parent = getClassFromPackage(
                    superName.getPackageName().toString(), superName.getSimpleName());
            parent.generateFieldOrder();
            codeFieldOrder.addAll(parent.codeFieldOrder);

            for (final FieldScope fieldScope : nonStaticFields) {
                if (codeFieldOrder.contains(fieldScope)) {
                    final LinkedHashSet<FieldScope> hashSet = new LinkedHashSet<>();
                    for (final FieldScope orderedFieldScope : codeFieldOrder) {
                        if (fieldScope.equals(orderedFieldScope)) {
                            hashSet.add(fieldScope);
                        } else {
                            hashSet.add(orderedFieldScope);
                        }
                    }
                    codeFieldOrder = hashSet;
                } else {
                    codeFieldOrder.add(fieldScope);
                }
            }
        } else {
            System.err.println("Found class with more than one extends?");
            System.exit(42);
        }
    }

    public void generateMethodOrder() {
        if (classType != CLASS) return;
        if (codeMethodOrder.size() != 0) return;

        if (extendsTable == null || extendsTable.size() == 0) {
            codeMethodOrder.addAll(methodTable);
        } else if (extendsTable.size() == 1) {
            final Name superName = extendsTable.get(0);
            final ClassScope parent = getClassFromPackage(
                    superName.getPackageName().toString(), superName.getSimpleName());
            parent.generateMethodOrder();
            codeMethodOrder.addAll(parent.codeMethodOrder);

            for (final MethodScope methodScope : methodTable) {
                if (codeMethodOrder.contains(methodScope)) {
                    final LinkedHashSet<MethodScope> hashSet = new LinkedHashSet<>();
                    for (final MethodScope orderedMethodScope : codeMethodOrder) {
                        if (methodScope.equals(orderedMethodScope)) {
                            hashSet.add(methodScope);
                        } else {
                            hashSet.add(orderedMethodScope);
                        }
                    }
                    codeMethodOrder = hashSet;
                } else {
                    codeMethodOrder.add(methodScope);
                }
            }
        } else {
            System.err.println("Found class with more than one extends?");
            System.exit(42);
        }
    }

    public ArrayList<String> generateAllocationCode() {
        final ArrayList<String> code = new ArrayList<>();

        code.add("mov eax, " + getWordSize() + " ; Number of bytes allocated.");
        code.addAll(generatePrologueCode());
        code.add("call __malloc");
        code.addAll(generateEpilogueCode());
        code.add("mov [eax], " + callVtableLabel());

        return code;
    }

    public int getNonStaticFieldOffset(final FieldScope target) {
        int it = 4;
        for (final FieldScope fieldScope : codeFieldOrder) {
            if (fieldScope.equals(target)) {
                return it;
            }
            it += 4;
        }

        System.err.println("Could not find vtable offset for given field.");
        System.exit(42);

        return -1;
    }

    public int getVTableOffset(final MethodScope target) {
        int it = 8;
        for (final MethodScope methodScope : codeMethodOrder) {
            if (methodScope.equals(target)) {
                return it;
            }
            it += 4;
        }

        System.err.println("Could not find vtable offset for given method.");
        System.exit(42);

        return -1;
    }

    protected ConstructorScope getSuperConstructor() {
        if (classType != CLASS) return null;

        if (extendsTable == null || extendsTable.size() == 0) {
            return null;
        } else if (extendsTable.size() == 1) {
            final Name superName = extendsTable.get(0);
            final ClassScope parent = getClassFromPackage(
                    superName.getPackageName().toString(), superName.getSimpleName());
            return parent.getEmptyConstructor();
        } else {
            System.err.println("Found class with more than one extends?");
            System.exit(42);
            return null;
        }
    }

    private ConstructorScope getEmptyConstructor() {
        for (final ConstructorScope constructorScope : constructorTable) {
            if (constructorScope.parameters == null
                    || constructorScope.parameters.size() == 0) {
                return constructorScope;
            }
        }

        System.err.println("Found class with no empty constructor?");
        System.exit(42);
        return null;
    }

    public ConstructorScope getConstructorWithArgs(final Expression args) {
        if (args == null) {
            return getEmptyConstructor();
        }

        final ArrayList<Type> argTypes = new ArrayList<>();
        if (args instanceof ArgumentListExpression) {
            final ArgumentListExpression argList = (ArgumentListExpression) args;
            argList.arguments.forEach(e -> argTypes.add(e.type));
        } else {
            argTypes.add(args.type);
        }

        for (final ConstructorScope constructorScope : constructorTable) {
            if (constructorScope.matchesParameters(argTypes)) return constructorScope;
        }

        System.err.println("Could not identify given constructor; aborting!");
        System.exit(42);

        return null;
    }

    @Override
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();

        code.addAll(methodExternList);
        code.addAll(staticExternSet);

        methodTable.forEach(e -> code.add("global " + e.callLabel()));
        constructorTable.forEach(e -> code.add("global " + e.callLabel()));

        code.add("section .data");

        // Generate the class vtable.
        code.add(setVtableLabel());
        code.add("dd " + callSITLabel() + " ; Pointer to the SIT.");
        code.add("dd " + callSubtypeTableLabel() + " ; Pointer to the subtype table.");
        codeMethodOrder.forEach(e -> code.add("dd " + e.callLabel()));

        // Generates our method code.
        for (final MethodScope methodScope : methodTable) {
            if (methodScope.body != null) {
                code.addAll(methodScope.generatei386Code());
                code.add("");
            }
        }

        // Generates our constructor code.
        for (final ConstructorScope constructorScope : constructorTable) {
            if (constructorScope.body != null) {
                code.addAll(constructorScope.generatei386Code());
                code.add("");
            }
        }

        writeCodeToFile(this.name, code);

        return null;
    }

    public String setVtableLabel() {
        return generateClassLabel() + "_vtable:";
    }

    public String callVtableLabel() {
        return generateClassLabel() + "_vtable";
    }

    public String setSITLabel() {
        return generateClassLabel() + "_sitrow:";
    }

    public String callSITLabel() {
        return generateClassLabel() + "_sitrow";
    }

    public String setSubtypeTableLabel() {
        return generateClassLabel() + "_subtypeTable:";
    }

    public String callSubtypeTableLabel() {
        return generateClassLabel() + "_subtypeTable";
    }

    protected String generateClassLabel() {
        return generateFullyQualifiedName(name, packageName).getQualifiedName();
    }
}
