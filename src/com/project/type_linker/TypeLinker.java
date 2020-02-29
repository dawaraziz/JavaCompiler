package com.project.type_linker;

import com.project.environments.ClassScope;
import com.project.environments.ImportScope;
import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;
import com.project.environments.structure.Name;
import com.project.environments.structure.Type;
import com.project.parser.structure.ParserSymbol;
import com.project.scanner.structure.Kind;
import resources.Pair;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import static com.project.environments.ImportScope.IMPORT_TYPE.ON_DEMAND;
import static com.project.environments.ImportScope.IMPORT_TYPE.SINGLE;

public class TypeLinker {
    public static void disambiguate(final ASTHead astHead){
        // Get every Variable_ID and classify it as
        // PackageName, TypeName, ExpressionName, MethodName, PackageOrTypeName, or AmbiguousName
        ArrayList<ASTNode> nameNodes = astHead.unsafeGetHeadNode().findNodesWithKinds(Kind.VARIABLE_ID);
        for (ASTNode node : nameNodes){
            // Default all variable_id to typeName on first pass
            defaultToTypeName(node);
        }
        for (ASTNode node : nameNodes){
            changeIfExpressionName(node);
            changeIfMethodName(node);
            changeIfDeclOnDemand(node);
        }

        ArrayList<ASTNode> qualifiedNodes = astHead.unsafeGetHeadNode().findNodesWithLexeme("QUALIFIEDNAME");
        //PackageOrTypeName and AmbiguousName need to be found on second pass in qualified names
        for (ASTNode node : qualifiedNodes) {
            resolveQualifiedNames(node);
        }

    }

    public static boolean parentIsLexeme(ASTNode node, String lex){
        if (node.parent != null && node.parent.lexeme.equals(lex)){
            return true;
        }
        return false;
    }

    // If any upper scope of node is lex than return True
    public static boolean within(ASTNode node, String lex){
        while (node.parent != null){
            if (node.parent.lexeme.equals(lex)){
                return true;
            }
            node = node.parent;
        }
        return false;
    }

    public static void defaultToTypeName(ASTNode node){
        node.kind = Kind.TYPENAME;
    }

    public static void changeIfDeclOnDemand(ASTNode node){
        if (within(node, "TYPEIMPORTONDEMANDDECLARATION")){
            node.kind = Kind.PACKAGEORTYPENAME;
        }
    }

    public static void changeIfExpressionName(ASTNode node) {
        // As the qualifying expression in a qualified superclass constructor invocation (§8.8.5.1) - IRRELEVANT TO JOOS
        // ex - Primary.super ( ArgumentListopt ) ;

        // As the qualifying expression in a qualified class instance creation expression (§15.9) - IRRELEVANT TO JOOS
        // ex - Primary.new Identifier ( ArgumentListopt ) ClassBodyopt

        // As a PostfixExpression (§15.14) - IRRELEVANT TO JOOS

        if (node.kind.equals(Kind.TYPENAME)) {

            // As the array reference expression in an array access expression (§15.13)
            if (parentIsLexeme(node, "ARRAYACCESS")){
                node.kind = Kind.EXPRESSIONNAME;
            }

            // As the left-hand operand of an assignment operator (§15.26)
            else if (parentIsLexeme(node, "VARIABLEDECLARATORID")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            else if (parentIsLexeme(node, "VARIABLEDECLARATOR")){
                node.kind = Kind.EXPRESSIONNAME;
            }

            // in an assignment ex. r = 5;
            else if (parentIsLexeme(node, "ASSIGNMENT")){
                node.kind = Kind.EXPRESSIONNAME;
            }

            // in a return ex. return r;
            else if (parentIsLexeme(node, "RETURNSTATEMENT")){
                node.kind = Kind.EXPRESSIONNAME;
            }

            // Expression names are in EXPRESSIONS!
            if (parentIsLexeme(node, "ADDITIVEEXPRESSION")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "MULTIPLICATIVEEXPRESSION")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "EQUALITYEXPRESSION")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "IFTHENSTATEMENT")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "IFTHENELSESTATEMENT")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "CONDITIONALANDEXPRESSION")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "CONDITIONALOREXPRESSION")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "UNARYEXPRESSIONNOTPLUSMINUS")){
                node.kind = Kind.EXPRESSIONNAME;
            }

            if (parentIsLexeme(node, "ANDEXPRESSION")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "EXCLUSIVEOREXPRESSION")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "UNARYEXPRESSION")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "WHILESTATEMENT")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "FORSTATEMENT")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "FORSTATEMENTNOSHORTIF")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "WHILESTATEMENTNOSHORTIF")){
                node.kind = Kind.EXPRESSIONNAME;
            }


            //TODO: I don't think you can declare anything in an argument list
            if (parentIsLexeme(node, "ARGUMENTLIST")){
                node.kind = Kind.EXPRESSIONNAME;
            }


            //TODO: NOT SURE IF THIS IS CORRECT
            if (parentIsLexeme(node, "QUALIFIEDNAME")) {
                if (within(node, "CLASSBODY") && !(node.parent.parent != null && node.parent.parent.lexeme.equals("LOCALVARIABLEDECLARATION"))){
                    node.kind = Kind.EXPRESSIONNAME;
                }
            }

            // Field access is Expression
            if (parentIsLexeme(node, "FIELDACCESS")){
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (parentIsLexeme(node, "PRIMARYNONEWARRAY")){
                node.kind = Kind.EXPRESSIONNAME;
            }

            // ex. j in char[] ret2 = new char[j];
            if (parentIsLexeme(node, "DIMEXPR")){
                node.kind = Kind.EXPRESSIONNAME;
            }


            // In the arguments of a class instance creation expression i.e after the (
            // CLASSINSTANCECREATIONEXPRESSION
            if (parentIsLexeme(node, "CLASSINSTANCECREATIONEXPRESSION")){
                ArrayList<ASTNode> children = node.parent.children;
                for (ASTNode child: children){
                    if (child.kind == Kind.PAREN_OPEN){
                        int paren_idx = children.indexOf(child);
                        int node_idx = children.indexOf(node);
                        if (node_idx < paren_idx){
                            node.kind = Kind.EXPRESSIONNAME;
                        }
                    }
                }
            }


            //If it is in a RELATIONALEXPRESSION and not after instanceof
            if (parentIsLexeme(node, "RELATIONALEXPRESSION")){
                node.kind = Kind.EXPRESSIONNAME;
                //if it comes after an instanceof though, it is a TypeName still
                //we must check the children of the parent to figure this out
                ArrayList<ASTNode> children = node.parent.children;
                for (ASTNode child: children){
                    if (child.kind == Kind.INSTANCEOF){
                        int idx = children.indexOf(child);
                        if (children.get(idx-1) == node){
                            node.kind = Kind.TYPENAME;
                        }
                    }
                }
            }

            // After Cast (Point)e;
            if (parentIsLexeme(node, "CASTEXPRESSION")){
                //if it comes after the closing bracket in a cast expression
                ArrayList<ASTNode> children = node.parent.children;
                for (ASTNode child: children){
                    if (child.kind == Kind.PAREN_CLOSE){
                        int idx = children.indexOf(child);
                        if (children.get(idx-1) == node){
                            node.kind = Kind.EXPRESSIONNAME;
                        }
                    }
                }
            }

        }
    }

    public static void changeIfMethodName(ASTNode node) {
        // Before the "(" in a method invocation expression
        if (node.kind.equals(Kind.TYPENAME)) {
            if (parentIsLexeme(node, "METHODINVOCATION")){
                node.kind = Kind.METHODNAME;
            }
            if (node.parent.parent != null && node.parent.parent.lexeme.equals("METHODINVOCATION")) {
                if (node.parent != null && (node.parent.lexeme.equals("QUALIFIEDNAME"))) {
                    // If it's the end of the qualified name it is a methodname
                    ASTNode last = node.parent.children.get(0);
                    if (node == last) {
                        node.kind = Kind.METHODNAME;
                    }
                }
            }

            // CHECK - Before the "(" in a method declarator - I added this one not in spec but has to be right?
            if (parentIsLexeme(node, "METHODDECLARATOR")){
                node.kind = Kind.METHODNAME;
            }

            if (parentIsLexeme(node, "CONSTRUCTORDECLARATOR")){
                node.kind = Kind.METHODNAME;
            }
        }
    }

    public static void resolveQualifiedNames(ASTNode node){
        // To the left of the "." in a qualified TypeName
        if (node.lexeme.equals("QUALIFIEDNAME")){
            ASTNode last = node.children.get(0);
            if (last.kind == Kind.TYPENAME) {
                for (int i = 2; i < node.children.size(); i+=2) {
                    ASTNode curr = node.children.get(i);
                    curr.kind = Kind.PACKAGEORTYPENAME;
                }
            }
            else {
                for (int i = 2; i < node.children.size(); i+=2) {
                    ASTNode curr = node.children.get(i);
                    curr.kind = Kind.AMBIGUOUSNAME;
                }
            }
        }

    }

    // Go through AST checking scopes of variables by using a stack of lists of variables
    // Every time we see open bracket add a new scope and every time we see a close pop one off
    public static void checkVariableDeclarationScopes(ASTHead astHead){
        Stack<Stack<ArrayList<String>>> scopesStack = new Stack<>();
        Stack<ArrayList<String>> topScopeStack = new Stack<>();
        scopesStack.add(topScopeStack);
        ASTNode node = astHead.unsafeGetHeadNode();

        final Stack<ASTNode> stack = new Stack<>();
        stack.add(node);
        while(!stack.empty()) {
            ASTNode curr = stack.pop();
            Stack<ArrayList<String>> currentScope = scopesStack.peek();
            // If we're in a new method/Constructor it's a new scope
            if (curr.lexeme.equals("CONSTRUCTORDECLARATOR") || curr.lexeme.equals("METHODDECLARATION")) {
                scopesStack.add(new Stack<>());
                scopesStack.peek().push(new ArrayList<>());
            }
            // New Scope add a new array to scope stack
            else if (curr.kind == Kind.CURLY_BRACKET_OPEN) {
                currentScope.push(new ArrayList<>());
            }
            // Moved up a scope pop off scopeStack
            else if (curr.kind == Kind.CURLY_BRACKET_CLOSE){
                currentScope.pop();
                if(currentScope.size() == 0){
                    scopesStack.pop();
                }
            }
            else if (curr.lexeme.equals("VARIABLEDECLARATORID")){
                ArrayList<ASTNode> variables = curr.getDirectChildrenWithKinds("EXPRESSIONNAME");
                for (ASTNode n : variables){
                    String newVar = n.lexeme;
                    // Check name is not within upper scope
                    for (ArrayList<String> vars : currentScope){
                        for (String oldVar : vars){
                            if (newVar.equals(oldVar)){
                                System.err.println("Redeclaration of variable: "+newVar);
                                System.exit(42);
                            }
                        }
                    }
                    // If not add it to the latest scope
                    if (currentScope.size() < 1){
                        System.exit(42);
                    }
                    currentScope.peek().add(newVar);
                }
            }
            for (ASTNode child : curr.children) {
                stack.add(child);
            }
        }
    }

    public static void checkSingleImports(final ClassScope javaClass, final HashMap<String, PackageScope> packages) {
        for (ImportScope importScope : javaClass.imports) {
            if (importScope.type == SINGLE) {
                boolean exists = false;
                String importPackage = importScope.name.getPackageName();
                String importClass = importScope.name.getActualSimpleName();
                for (Map.Entry<String, PackageScope> pkgEntry : packages.entrySet()) {
                    if (importPackage.equals(pkgEntry.getKey())) {
                        if (pkgEntry.getValue().containsClass(importClass)) {
                            exists = true;
                        }
                    }
                }
                if (exists == false) {
                    System.err.println("Imported Single Class " + importScope.name.getQualifiedName() + " does not exist");
                    System.exit(42);
                }
            }
        }
    }


    // Given a java class makes sure its on demand imports properly resolve
    public static void checkOnDemandImports(final ClassScope javaClass, final HashMap<String, PackageScope> packages){
            // Check imported name exists or is a prefix or a name
            for (ImportScope importScope : javaClass.imports){
                if(importScope.type == ON_DEMAND){
                    boolean exists = false;
                    for (String pkgName : packages.keySet()){
                        if (importScope.name.getQualifiedName().equals(pkgName)){
                            exists = true;
                        }
                        // A legal prefix must be all the way to a dot
                        String legalPrefix = importScope.name.getQualifiedName() + ".";
                        if (pkgName.startsWith(legalPrefix)){
                            System.out.println("PREFIX! " + importScope.name.getQualifiedName());
                            exists = true;
                        }
                    }
                    if (exists == false){
                        System.err.println("Imported on demand package " + importScope.name.getQualifiedName() + " does not exist");
                        System.exit(42);
                    }
                }
            }

            // Check if any two on demand imports have a conflicting class
            // ONLY COUNTS IF WE USE THE CLASS IN A WAY THAT CAN CONFLICT
            System.out.println("Types used in : " + javaClass.name);
            for (Name name : javaClass.usedTypeNames){
                System.out.println("Name$ " + name.getQualifiedName());
            }
            for (int i = 0; i < javaClass.imports.size(); i++){
                for (int j = i+1; j < javaClass.imports.size(); j++){
                    ImportScope importScope1 = javaClass.imports.get(i);
                    ImportScope importScope2 = javaClass.imports.get(j);

                    String import1Name = importScope1.name.getQualifiedName();
                    String import2Name = importScope2.name.getQualifiedName();

                    // Both on demand and not a prefix of one another
                    if(importScope1.type == ON_DEMAND && importScope2.type == ON_DEMAND &&
                    !import1Name.startsWith(import2Name) && !import2Name.startsWith(import1Name)){
                        System.out.println(importScope1.name + " and# " + importScope2.name);

                        // Check classes within each import package for conflict
                        // For an importScope i need all packages that it is a prefix of
                        ArrayList<String> matched1 = new ArrayList<>();
                        ArrayList<String> matched2 = new ArrayList<>();

                        for (String s : packages.keySet()){
                            if (s.startsWith(import1Name)){
                                matched1.add(s);
                            }
                            if (s.startsWith(import2Name)){
                                matched2.add(s);
                            }
                        }

                        // For all combinations of packages the import on demands resolve to check
                        // that their classes don't clash, if they do, it is only a problem if the type is used
                        for (String pkg1Name : matched1){
                            for (String pkg2Name : matched2) {
                                for (ClassScope c : packages.get(pkg1Name).classes) {
                                    for (ClassScope c2 : packages.get(pkg2Name).classes) {
                                        if (c.name.equals(c2.name)) {
                                            if (javaClass.usedTypeNameStrings.contains(c.name)) {
                                                //also make sure there is no single class import for this
//                                                if(javaClass.hasSingleTypeImportOfClass(c.name)) {
                                                    System.err.println("Two on demand imports " + importScope1.name + " and " + importScope2.name + " have a conflicting class " + c.name);
                                                    System.exit(42);
//                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }

    public static void checkPackageNames(final ClassScope javaClass, final HashMap<String, PackageScope> packages){
        // Package - Classname clash ex. foo.bar package name illegal if there is a qualified classname foo.bar
        for (String pkgName : packages.keySet()){
            System.out.println(pkgName + " $: " + javaClass.packageName.getQualifiedName()+'.'+javaClass.name);
            if(pkgName.equals(javaClass.packageName.getQualifiedName()+'.'+javaClass.name)){
                System.err.println("Package Name " + pkgName + " clashes with qualified class name");
                System.exit(42);
            }
            //If classname prefix of package fail
            if(pkgName.startsWith(javaClass.packageName.getQualifiedName()+'.'+javaClass.name)){
                System.err.println("Package Name " + pkgName + " clashes with qualified class name (Classname is Prefix)");
                System.exit(42);
            }
        }
    }

    public static void checkTypesAreImported(final ClassScope javaClass, final HashMap<String, PackageScope> packages){
        // for every typename in javaclass we import the appropriate
        for (Name typeName : javaClass.usedTypeNames) {
            // for each type check that one of the imported packages contains it
            boolean imported = false;
            String type = typeName.getSimpleName();

            //if its a qualified name then just make sure the package exists and it contains the class
            if(typeName.isNotSimpleName()){
                String qualifiedPackage = typeName.getPackageName();
                String qualifiedClass = typeName.getActualSimpleName();
                PackageScope pkgScope = packages.get(qualifiedPackage);
                if (pkgScope != null && pkgScope.containsClass(qualifiedClass)){
                    imported = true;
                }
            }
            else {
                ArrayList<ImportScope> importsAndSelf = new ArrayList<>();
                importsAndSelf.addAll(javaClass.imports);
                // Add own package if it is part of one so it can access other classes in its package
                if (!javaClass.packageName.equals("default#")) {
                    importsAndSelf.add(new ImportScope(ON_DEMAND, javaClass.packageName, null));
                }
                for (ImportScope importScope : importsAndSelf) {

                    // For Import on demand
                    if (importScope.type.equals(ON_DEMAND)) {
                        String importPackage = importScope.name.getQualifiedName();
                        PackageScope pkgScope = packages.get(importPackage);
                        if (pkgScope != null) {
                            for (ClassScope importedClass : pkgScope.classes) {
                                // Equals simple type or the fully qualified name
                                if (type.equals(importedClass.name)) {
                                    imported = true;
                                }
                            }
                        }
                    } else {
                        //Single Type Import
                        String importedType = importScope.name.getSimpleName();
                        System.out.println(importedType);
                        // Equals simple Type or fully qualified
                        if (type.equals(importedType) || type.equals(importScope.name.getQualifiedName())) {
                            imported = true;
                        }

                    }


                }
            }
            if (!imported) {
                System.err.println(type + " was not imported");
                System.exit(42);
            }
        }

    }

    public static void link(final ArrayList<ClassScope> classTable, final HashMap<String, ClassScope> classMap, final HashMap<String, PackageScope> packages){
        for (ClassScope javaClass : classTable) {

            //Class name can't be package name
//            if (javaClass.name.equals(javaClass.packageName.getActualSimpleName())){
//                System.err.println("Class name is the same as package name");
//                System.exit(42);
//            }

            // Check no import clashes with class or interface definitions
            for (ImportScope imp : javaClass.imports) {
                if (imp.type == SINGLE && imp.name.getClassName().equals(javaClass.name) &&
                    !imp.name.checkPackageMatch(javaClass.packageName)){
                    System.err.println("Class import same as class or interface declared");
                    System.exit(42);
                }
            }

            //Check if two single-type-import declarations
            for (int i = 0; i < javaClass.imports.size(); i++) {
                for (int j = i+1; j < javaClass.imports.size(); j++) {
                    ImportScope import1 = javaClass.imports.get(i);
                    ImportScope import2 = javaClass.imports.get(j);
                    if (import1.name.getClassName().equals(import2.name.getClassName())
                            && import1.type != ON_DEMAND && import2.type != ON_DEMAND) {
                        if (!import1.name.getQualifiedName().equals(import2.name.getQualifiedName())) {
                            System.err.println("Two single-type imports clashed");
                            System.exit(42);
                        }
                    }
                }
            }

            // All type names must resolve to some class or interface declared in some file listed on the Joos command line.
            // Get all typeNames from the AST - ignore any whose parent is a package declaration
            ASTHead astHead = javaClass.ast;
            ArrayList<ASTNode> nameNodes = astHead.unsafeGetHeadNode().findNodesWithKinds(Kind.TYPENAME);
            nameNodes = nameNodes.stream().filter(n -> !within(n, "PACKAGEDECLARATION")).collect(Collectors.toCollection(ArrayList::new));

            // for each typeName see if it is an object in our classTable
            for (ASTNode node : nameNodes){
                String name = node.lexeme;
                boolean found = false;
                for (int i = 0; i < classTable.size(); ++i){
                    if (classTable.get(i).name.equals(name)) {
                        found = true;
                    }
                }
                if (!found){
                    System.err.println("Class Table does not contain "+name);
                    System.exit(42);
                }
            }

            // All simple type names must resolve to a unique class or interface.


            // Check all on demand imports resolve
            checkOnDemandImports(javaClass, packages);

            // Check all on demand imports resolve
            checkSingleImports(javaClass, packages);

            // Check all on demand imports resolve
            checkPackageNames(javaClass, packages);

            // Check all types used are properly imported
            checkTypesAreImported(javaClass, packages);

            // Deal with variable redeclaration within scopes
            checkVariableDeclarationScopes(astHead);

        }
        return;
    }
}




