package com.project.type_linker;

import com.project.environments.ClassScope;
import com.project.environments.ImportScope;
import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;
import com.project.environments.structure.Name;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.project.environments.ImportScope.IMPORT_TYPE.ON_DEMAND;
import static com.project.environments.ImportScope.IMPORT_TYPE.SINGLE;

public class TypeLinker {
    public static void assignTypesToNames(final ASTHead astHead) {
        // Get every Variable_ID and classify it as
        // PackageName, TypeName, ExpressionName, MethodName, PackageOrTypeName, or AmbiguousName
        final ArrayList<ASTNode> nameNodes = astHead.getVariableIDNodes();

        for (final ASTNode node : nameNodes) {
            node.defaultToTypeName();
        }

        for (final ASTNode node : nameNodes) {
            changeIfExpressionName(node);
            changeIfMethodName(node);
            changeIfDeclOnDemand(node);
        }

        //PackageOrTypeName and AmbiguousName need to be found on second pass in qualified names
        for (final ASTNode node : astHead.getQualifiedNameNodes()) {
            resolveQualifiedNames(node);
        }

    }

    private static void changeIfDeclOnDemand(final ASTNode node) {
        if (node.within("TYPEIMPORTONDEMANDDECLARATION")) {
            node.kind = Kind.PACKAGEORTYPENAME;
        }
    }

    private static void changeIfExpressionName(final ASTNode node) {
        // As the qualifying expression in a qualified superclass constructor invocation (§8.8.5.1) - IRRELEVANT TO JOOS
        // ex - Primary.super ( ArgumentListopt ) ;

        // As the qualifying expression in a qualified class instance creation expression (§15.9) - IRRELEVANT TO JOOS
        // ex - Primary.new Identifier ( ArgumentListopt ) ClassBodyopt

        // As a PostfixExpression (§15.14) - IRRELEVANT TO JOOS

        if (node.kind.equals(Kind.TYPENAME)) {

            // As the array reference expression in an array access expression (§15.13)
            if (node.parentIsLexeme("ARRAYACCESS")) {
                node.kind = Kind.EXPRESSIONNAME;
            }

            // As the left-hand operand of an assignment operator (§15.26)
            else if (node.parentIsLexeme("VARIABLEDECLARATORID")) {
                node.kind = Kind.EXPRESSIONNAME;
            } else if (node.parentIsLexeme("VARIABLEDECLARATOR")) {
                node.kind = Kind.EXPRESSIONNAME;
            }

            // in an assignment ex. r = 5;
            else if (node.parentIsLexeme("ASSIGNMENT")) {
                node.kind = Kind.EXPRESSIONNAME;
            }

            // in a return ex. return r;
            else if (node.parentIsLexeme("RETURNSTATEMENT")) {
                node.kind = Kind.EXPRESSIONNAME;
            }

            // Expression names are in EXPRESSIONS!
            if (node.parentIsLexeme("ADDITIVEEXPRESSION")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("MULTIPLICATIVEEXPRESSION")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("EQUALITYEXPRESSION")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("IFTHENSTATEMENT")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("IFTHENELSESTATEMENT")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("CONDITIONALANDEXPRESSION")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("CONDITIONALOREXPRESSION")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("UNARYEXPRESSIONNOTPLUSMINUS")) {
                node.kind = Kind.EXPRESSIONNAME;
            }

            if (node.parentIsLexeme("ANDEXPRESSION")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("EXCLUSIVEOREXPRESSION")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("UNARYEXPRESSION")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("WHILESTATEMENT")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("FORSTATEMENT")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("FORSTATEMENTNOSHORTIF")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("WHILESTATEMENTNOSHORTIF")) {
                node.kind = Kind.EXPRESSIONNAME;
            }


            //TODO: I don't think you can declare anything in an argument list
            if (node.parentIsLexeme("ARGUMENTLIST")) {
                node.kind = Kind.EXPRESSIONNAME;
            }


            //TODO: NOT SURE IF THIS IS CORRECT
            if (node.parentIsLexeme("QUALIFIEDNAME")) {
                if (node.within("CLASSBODY") && !(node.parent.parent != null && node.parent.parent.lexeme.equals("LOCALVARIABLEDECLARATION"))) {
                    node.kind = Kind.EXPRESSIONNAME;
                }
            }

            // Field access is Expression
            if (node.parentIsLexeme("FIELDACCESS")) {
                node.kind = Kind.EXPRESSIONNAME;
            }
            if (node.parentIsLexeme("PRIMARYNONEWARRAY")) {
                node.kind = Kind.EXPRESSIONNAME;
            }

            // ex. j in char[] ret2 = new char[j];
            if (node.parentIsLexeme("DIMEXPR")) {
                node.kind = Kind.EXPRESSIONNAME;
            }


            // In the arguments of a class instance creation expression i.e after the (
            // CLASSINSTANCECREATIONEXPRESSION
            if (node.parentIsLexeme("CLASSINSTANCECREATIONEXPRESSION")) {
                final ArrayList<ASTNode> children = node.parent.children;
                for (final ASTNode child : children) {
                    if (child.kind == Kind.PAREN_OPEN) {
                        final int paren_idx = children.indexOf(child);
                        final int node_idx = children.indexOf(node);
                        if (node_idx < paren_idx) {
                            node.kind = Kind.EXPRESSIONNAME;
                        }
                    }
                }
            }


            //If it is in a RELATIONALEXPRESSION and not after instanceof
            if (node.parentIsLexeme("RELATIONALEXPRESSION")) {
                node.kind = Kind.EXPRESSIONNAME;
                //if it comes after an instanceof though, it is a TypeName still
                //we must check the children of the parent to figure this out
                final ArrayList<ASTNode> children = node.parent.children;
                for (final ASTNode child : children) {
                    if (child.kind == Kind.INSTANCEOF) {
                        final int idx = children.indexOf(child);
                        if (children.get(idx - 1) == node) {
                            node.kind = Kind.TYPENAME;
                        }
                    }
                }
            }

            // After Cast (Point)e;
            if (node.parentIsLexeme("CASTEXPRESSION")) {
                //if it comes after the closing bracket in a cast expression
                final ArrayList<ASTNode> children = node.parent.children;
                for (final ASTNode child : children) {
                    if (child.kind == Kind.PAREN_CLOSE) {
                        final int idx = children.indexOf(child);
                        if (children.get(idx - 1) == node) {
                            node.kind = Kind.EXPRESSIONNAME;
                        }
                    }
                }
            }

        }
    }

    private static void changeIfMethodName(final ASTNode node) {
        // Before the "(" in a method invocation expression
        if (node.kind.equals(Kind.TYPENAME)) {
            if (node.parentIsLexeme("METHODINVOCATION")) {
                node.kind = Kind.METHODNAME;
            }
            if (node.parent.parent != null && node.parent.parent.lexeme.equals("METHODINVOCATION")) {
                if (node.parent.lexeme.equals("QUALIFIEDNAME")) {
                    // If it's the end of the qualified name it is a methodname
                    final ASTNode last = node.parent.children.get(0);
                    if (node == last) {
                        node.kind = Kind.METHODNAME;
                    }
                }
            }

            // CHECK - Before the "(" in a method declarator - I added this one not in spec but has to be right?
            if (node.parentIsLexeme("METHODDECLARATOR")) {
                node.kind = Kind.METHODNAME;
            }

            if (node.parentIsLexeme("CONSTRUCTORDECLARATOR")) {
                node.kind = Kind.METHODNAME;
            }
        }
    }

    private static void resolveQualifiedNames(final ASTNode node) {
        // To the left of the "." in a qualified TypeName
        if (node.lexeme.equals("QUALIFIEDNAME")) {
            final ASTNode last = node.children.get(0);
            if (last.kind == Kind.TYPENAME) {
                for (int i = 2; i < node.children.size(); i += 2) {
                    final ASTNode curr = node.children.get(i);
                    curr.kind = Kind.PACKAGEORTYPENAME;
                }
            } else {
                for (int i = 2; i < node.children.size(); i += 2) {
                    final ASTNode curr = node.children.get(i);
                    curr.kind = Kind.AMBIGUOUSNAME;
                }
            }
        }

    }

    // Go through AST checking scopes of variables by using a stack of lists of variables
    // Every time we see open bracket add a new scope and every time we see a close pop one off
    private static void checkVariableDeclarationScopes(final ASTHead astHead) {
        final Stack<Stack<ArrayList<String>>> scopesStack = new Stack<>();
        final Stack<ArrayList<String>> topScopeStack = new Stack<>();
        scopesStack.add(topScopeStack);
        final ASTNode node = astHead.unsafeGetHeadNode();

        final Stack<ASTNode> stack = new Stack<>();
        stack.add(node);
        while (!stack.empty()) {
            final ASTNode curr = stack.pop();
            final Stack<ArrayList<String>> currentScope = scopesStack.peek();
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
            else if (curr.kind == Kind.CURLY_BRACKET_CLOSE) {
                currentScope.pop();
                if (currentScope.size() == 0) {
                    scopesStack.pop();
                }
            } else if (curr.lexeme.equals("VARIABLEDECLARATORID")) {
                final ArrayList<ASTNode> variables = curr.getDirectChildrenWithKinds("EXPRESSIONNAME");
                for (final ASTNode n : variables) {
                    final String newVar = n.lexeme;
                    // Check name is not within upper scope
                    for (final ArrayList<String> vars : currentScope) {
                        for (final String oldVar : vars) {
                            if (newVar.equals(oldVar)) {
                                System.err.println("Redeclaration of variable: " + newVar);
                                System.exit(42);
                            }
                        }
                    }
                    // If not add it to the latest scope
                    if (currentScope.size() < 1) {
                        System.exit(42);
                    }
                    currentScope.peek().add(newVar);
                }
            }
            stack.addAll(curr.children);
        }
    }

    private static void checkSingleImports(final ClassScope javaClass, final HashMap<String, PackageScope> packages) {
        for (final ImportScope importScope : javaClass.imports) {
            if (importScope.type == SINGLE) {
                boolean exists = false;
                final String importPackage = importScope.name.getPackageString();
                final String importClass = importScope.name.getActualSimpleName();
                for (final Map.Entry<String, PackageScope> pkgEntry : packages.entrySet()) {
                    if (importPackage.equals(pkgEntry.getKey())) {
                        if (pkgEntry.getValue().containsClass(importClass)) {
                            exists = true;
                        }
                    }
                }
                if (!exists) {
                    System.err.println("Imported Single Class " + importScope.name.getQualifiedName() + " does not exist");
                    System.exit(42);
                }
            }
        }
    }


    // Given a java class makes sure its on demand imports properly resolve
    private static void checkOnDemandImports(final ClassScope javaClass, final HashMap<String, PackageScope> packages) {
        // Check imported name exists or is a prefix or a name
        for (final ImportScope importScope : javaClass.imports) {
            if (importScope.type == ON_DEMAND) {
                boolean exists = false;
                for (final String pkgName : packages.keySet()) {
                    if (importScope.name.getQualifiedName().equals(pkgName)) {
                        exists = true;
                    }
                    // A legal prefix must be all the way to a dot
                    final String legalPrefix = importScope.name.getQualifiedName() + ".";
                    if (pkgName.startsWith(legalPrefix)) {
                        System.out.println("PREFIX! " + importScope.name.getQualifiedName());
                        exists = true;
                    }
                }
                if (!exists) {
                    System.err.println("Imported on demand package " + importScope.name.getQualifiedName() + " does not exist");
                    System.exit(42);
                }
            }
        }
    }

    private static void checkPackageNames(final ClassScope javaClass, final HashMap<String, PackageScope> packages) {
        // Package - Classname clash ex. foo.bar package name illegal if there is a qualified classname foo.bar
        for (final String pkgName : packages.keySet()) {
            System.out.println(pkgName + " $: " + javaClass.packageName.getQualifiedName() + '.' + javaClass.name);
            if (pkgName.equals(javaClass.packageName.getQualifiedName() + '.' + javaClass.name)) {
                System.err.println("Package Name " + pkgName + " clashes with qualified class name");
                System.exit(42);
            }
            //If classname prefix of package fail
            if (pkgName.startsWith(javaClass.packageName.getQualifiedName() + '.' + javaClass.name)) {
                System.err.println("Package Name " + pkgName + " clashes with qualified class name (Classname is Prefix)");
                System.exit(42);
            }
        }
    }

    private static void checkTypesAreImported(final ClassScope javaClass, final HashMap<String, PackageScope> packages) {
        // for every typename in javaclass we import the appropriate
        for (final Name typeName : javaClass.usedTypeNames) {
            // for each type check that one of the imported packages contains it
            boolean imported = false;
            final String type = typeName.getSimpleName();

            //if its a qualified name then just make sure the package exists and it contains the class
            if (typeName.isNotSimpleName()) {
                final String qualifiedPackage = typeName.getPackageString();
                final String qualifiedClass = typeName.getActualSimpleName();
                final PackageScope pkgScope = packages.get(qualifiedPackage);
                if (pkgScope != null && pkgScope.containsClass(qualifiedClass)) {
                    imported = true;
                }
            } else {
                final ArrayList<ImportScope> importsAndSelf = new ArrayList<>(javaClass.imports);
                // Add own package if it is part of one so it can access other classes in its package
                if (!javaClass.packageName.isDefault()) {
                    importsAndSelf.add(new ImportScope(ON_DEMAND, javaClass.packageName, null));
                }
                for (final ImportScope importScope : importsAndSelf) {

                    // For Import on demand
                    if (importScope.type.equals(ON_DEMAND)) {
                        final String importPackage = importScope.name.getQualifiedName();
                        final PackageScope pkgScope = packages.get(importPackage);
                        if (pkgScope != null) {
                            for (final ClassScope importedClass : pkgScope.classes) {
                                // Equals simple type or the fully qualified name
                                if (type.equals(importedClass.name)) {
                                    imported = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        //Single Type Import
                        final String importedType = importScope.name.getSimpleName();
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

    public static void link(final ArrayList<ClassScope> classTable, final HashMap<String, PackageScope> packages) {
        for (final ClassScope javaClass : classTable) {

            //Class name can't be package name
//            if (javaClass.name.equals(javaClass.packageName.getActualSimpleName())){
//                System.err.println("Class name is the same as package name");
//                System.exit(42);
//            }

            // Check no import clashes with class or interface definitions
            for (final ImportScope imp : javaClass.imports) {
                if (imp.type == SINGLE && imp.name.getClassName().equals(javaClass.name) &&
                        !imp.name.checkPackageMatch(javaClass.packageName)) {
                    System.err.println("Class import same as class or interface declared");
                    System.exit(42);
                }
            }

            //Check if two single-type-import declarations
            for (int i = 0; i < javaClass.imports.size(); i++) {
                for (int j = i + 1; j < javaClass.imports.size(); j++) {
                    final ImportScope import1 = javaClass.imports.get(i);
                    final ImportScope import2 = javaClass.imports.get(j);
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
            final ASTHead astHead = javaClass.ast;
            ArrayList<ASTNode> nameNodes = astHead.unsafeGetHeadNode().findNodesWithKinds(Kind.TYPENAME);
            nameNodes = nameNodes.stream().filter(n -> !n.within("PACKAGEDECLARATION")).collect(Collectors.toCollection(ArrayList::new));

            // for each typeName see if it is an object in our classTable
            for (final ASTNode node : nameNodes) {
                final String name = node.lexeme;
                boolean found = false;
                for (final ClassScope classScope : classTable) {
                    if (classScope.name.equals(name)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.err.println("Class Table does not contain " + name);
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
    }
}




