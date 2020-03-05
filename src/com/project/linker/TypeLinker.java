package com.project.linker;

import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.ImportScope;
import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;
import com.project.environments.structure.Name;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;

import static com.project.environments.scopes.ImportScope.IMPORT_TYPE.ON_DEMAND;

public class TypeLinker {

    /**
     * Get every Variable_ID and classify it as PackageName, TypeName, ExpressionName,
     * MethodName, PackageOrTypeName, or AmbiguousName
     */
    public static void assignTypesToNames(final ASTHead astHead) {

        final ArrayList<ASTNode> nameNodes = astHead.getVariableIDNodes();

        // Sets all the nodes to TypeName by default.
        for (final ASTNode node : nameNodes) {
            node.defaultToTypeName();
        }

        for (final ASTNode node : nameNodes) {
            setExpressionTypes(node);
            setMethodTypes(node);
            setOnDemandImportTypes(node);
        }

        // Check for PackageOrTypeName and AmbiguousName in qualified names.
        for (final ASTNode node : astHead.getQualifiedNameNodes()) {
            resolveQualifiedNames(node);
        }
    }

    private static void setOnDemandImportTypes(final ASTNode node) {
        if (node.within("TYPEIMPORTONDEMANDDECLARATION"))
            node.kind = Kind.PACKAGEORTYPENAME;
    }

    private static void setExpressionTypes(final ASTNode node) {
        if (!node.kind.equals(Kind.TYPENAME) || node.parent == null) return;
        final ArrayList<ASTNode> children = node.parent.children;

        // As the qualifying expression in a qualified superclass constructor invocation (§8.8.5.1) - IRRELEVANT TO JOOS
        // ex - Primary.super ( ArgumentListopt ) ;

        // As the qualifying expression in a qualified class instance creation expression (§15.9) - IRRELEVANT TO JOOS
        // ex - Primary.new Identifier ( ArgumentListopt ) ClassBodyopt

        // As a PostfixExpression (§15.14) - IRRELEVANT TO JOOS

        switch (node.parent.lexeme) {
            case "ARRAYACCESS": // As the array reference expression in an array access expression (§15.13)
            case "VARIABLEDECLARATORID": // As the left-hand operand of an assignment operator (§15.26)
            case "VARIABLEDECLARATOR": // As the left-hand operand of an assignment operator (§15.26)
            case "ASSIGNMENT": // in an assignment ex. r = 5;
            case "RETURNSTATEMENT":
            case "ADDITIVEEXPRESSION": // Expression names are in EXPRESSIONS!
            case "MULTIPLICATIVEEXPRESSION":
            case "EQUALITYEXPRESSION":
            case "IFTHENSTATEMENT":
            case "IFTHENELSESTATEMENT":
            case "CONDITIONALANDEXPRESSION":
            case "CONDITIONALOREXPRESSION":
            case "UNARYEXPRESSIONNOTPLUSMINUS":
            case "ANDEXPRESSION":
            case "EXCLUSIVEOREXPRESSION":
            case "UNARYEXPRESSION":
            case "WHILESTATEMENT":
            case "FORSTATEMENT":
            case "FORSTATEMENTNOSHORTIF":
            case "WHILESTATEMENTNOSHORTIF":
            case "ARGUMENTLIST": // TODO: I don't think you can declare anything in an argument list
            case "FIELDACCESS":// Field access is Expression.
            case "PRIMARYNONEWARRAY":
            case "DIMEXPR": // Ex. j in char[] ret2 = new char[j];
                node.kind = Kind.EXPRESSIONNAME;
                break;
            case "QUALIFIEDNAME": // TODO: NOT SURE IF THIS IS CORRECT
                if (node.within("CLASSBODY")
                        && !(node.parent.parent != null
                        && node.parent.parent.lexeme.equals("LOCALVARIABLEDECLARATION"))) {
                    node.kind = Kind.EXPRESSIONNAME;
                }
                break;
            case "CLASSINSTANCECREATIONEXPRESSION":

                // In the arguments of a class instance creation expression i.e after the (
                // CLASSINSTANCECREATIONEXPRESSION.
                if (children.stream().anyMatch(c -> c.kind == Kind.PAREN_OPEN
                        && children.indexOf(node) < children.indexOf(c))) {
                    node.kind = Kind.EXPRESSIONNAME;
                }
                break;
            case "RELATIONALEXPRESSION":

                // If it comes after an INSTANCEOF, it may be a TypeName.
                // Check the parent's children to confirm this.
                if (children.stream().anyMatch(c -> c.kind == Kind.INSTANCEOF
                        && children.get(children.indexOf(c) - 1) == node)) {
                    node.kind = Kind.TYPENAME;
                } else {
                    node.kind = Kind.EXPRESSIONNAME;
                }
                break;
            case "CASTEXPRESSION": // After Cast {(OBJECT) e};

                // If it comes after the closing bracket in a cast expression
                if (children.stream().anyMatch(c -> c.kind == Kind.PAREN_CLOSE
                        && children.get(children.indexOf(c) - 1) == node)) {
                    node.kind = Kind.EXPRESSIONNAME;
                }
                break;
        }
    }

    private static void setMethodTypes(final ASTNode node) {
        if (!node.kind.equals(Kind.TYPENAME) || node.parent == null) return;

        switch (node.parent.lexeme) {
            case "METHODINVOCATION":  // Before the "(" in a method invocation expression
            case "METHODDECLARATOR":
            case "CONSTRUCTORDECLARATOR":
                node.kind = Kind.METHODNAME;
                break;
            case "QUALIFIEDNAME":
                if (node.parent.parent != null
                        && node.parent.parent.lexeme.equals("METHODINVOCATION")
                        && node == node.parent.children.get(0)) {
                    node.kind = Kind.METHODNAME;
                }
        }
    }

    private static void resolveQualifiedNames(final ASTNode node) {
        if (!node.lexeme.equals("QUALIFIEDNAME")) return;

        // To the left of the "." in a qualified TypeName
        if (node.children.get(0).kind == Kind.TYPENAME) {
            node.children.stream()
                    .filter(c -> !c.kind.equals(Kind.DOT))
                    .forEach(c -> c.kind = Kind.PACKAGEORTYPENAME);
        } else {
            node.children.stream()
                    .filter(c -> !c.kind.equals(Kind.DOT))
                    .forEach(c -> c.kind = Kind.AMBIGUOUSNAME);
        }
    }

    // Go through AST checking scopes of variables by using a stack of lists of variables
    // Every time we see open bracket add a new scope and every time we see a close pop one off
    private static void checkVariableDeclarationScopes(final ASTHead astHead) {
        final ASTNode node = astHead.unsafeGetHeadNode();

        final Stack<ArrayList<String>> topScopeStack = new Stack<>();

        final Stack<Stack<ArrayList<String>>> scopesStack = new Stack<>();
        scopesStack.add(topScopeStack);

        final Stack<ASTNode> stack = new Stack<>();
        stack.add(node);

        while (!stack.empty()) {
            final ASTNode curr = stack.pop();
            final Stack<ArrayList<String>> currentScope = scopesStack.peek();

            switch (curr.lexeme) {
                case "CONSTRUCTORDECLARATOR": // If we're in a new method/Constructor it's a new scope
                case "METHODDECLARATION":
                    scopesStack.add(new Stack<>());
                    scopesStack.peek().push(new ArrayList<>());
                    break;
                case "VARIABLEDECLARATORID":
                    for (final ASTNode n : curr.getDirectChildrenWithKinds("EXPRESSIONNAME")) {
                        final String newVar = n.lexeme;

                        // Check name is not within upper scope
                        if (currentScope.stream()
                                .flatMap(Collection::stream)
                                .anyMatch(newVar::equals)) {
                            System.err.println("Redeclaration of variable: " + newVar);
                            System.exit(42);
                        }

                        if (currentScope.size() < 1) {
                            System.err.println("Found variable without scope; aborting!");
                            System.exit(42);
                        }

                        currentScope.peek().add(newVar);
                    }
            }

            if (curr.kind != null) {
                switch (curr.kind) {
                    case CURLY_BRACKET_OPEN: // New Scope add a new array to scope stack
                        currentScope.push(new ArrayList<>());
                        break;
                    case CURLY_BRACKET_CLOSE: // Moved up a scope pop off scopeStack
                        currentScope.pop();
                        if (currentScope.size() == 0) {
                            scopesStack.pop();
                        }
                }
            }

            stack.addAll(curr.children);
        }
    }

    private static void checkPackageNames(final ClassScope javaClass,
                                          final HashMap<String, PackageScope> packages) {
        // Package - Classname clash ex. foo.bar package name illegal if there is a qualified classname foo.bar
        for (final String pkgName : packages.keySet()) {
            System.out.println(pkgName + " $: " + javaClass.packageName.getQualifiedName() + '.' + javaClass.name);

            if (pkgName.equals(javaClass.packageName.getQualifiedName() + '.' + javaClass.name)) {
                System.err.println("Package Name " + pkgName + " clashes with qualified class name");
                System.exit(42);
            }

            // If classname prefix of package fail
            if (pkgName.startsWith(javaClass.packageName.getQualifiedName() + '.' + javaClass.name)) {
                System.err.println("Package Name " + pkgName + " clashes with qualified class name (Classname is Prefix)");
                System.exit(42);
            }
        }
    }

    private static void checkTypesAreImported(final ClassScope javaClass,
                                              final HashMap<String, PackageScope> packages) {
        for (final Name typeName : javaClass.usedTypeNames) {

            // If the name is already qualified, ensure it is contained.
            if (typeName.isNotSimpleName()) {
                final String qualifiedPackage = typeName.getPackageString();
                final String qualifiedClass = typeName.getActualSimpleName();
                final PackageScope pkgScope = packages.get(qualifiedPackage);

                if (pkgScope == null || !pkgScope.containsClass(qualifiedClass)) {
                    System.err.println("Found non-imported qualified name: " + typeName.getQualifiedName());
                    System.exit(42);
                }
            } else {
                final String type = typeName.getSimpleName();

                final ArrayList<ImportScope> importsAndSelf = new ArrayList<>(javaClass.imports);

                // TODO: Compiled error was here!
                // if (!javaClass.packageName.equals("default#"))
                importsAndSelf.add(new ImportScope(ON_DEMAND, javaClass.packageName, null));

                for (final ImportScope importScope : importsAndSelf) {
                    if (importScope.importType.equals(ON_DEMAND)) {
                        final String importPackage = importScope.name.getQualifiedName();
                        final PackageScope pkgScope = packages.get(importPackage);

                        if (pkgScope == null) continue;

                        if (pkgScope.classes.stream()
                                .anyMatch(c -> type.equals(c.name))) {
                            return;
                        }
                    } else {
                        if (type.equals(importScope.name.getSimpleName())
                                || type.equals(importScope.name.getQualifiedName())) {
                            return;
                        }
                    }
                }

                System.err.println("Found non-imported unqualified name: " + type);
                System.exit(42);
            }
        }
    }

    public static void link(final ArrayList<ClassScope> classTable,
                            final HashMap<String, PackageScope> packages) {
        for (final ClassScope javaClass : classTable) {

            // Check all on demand imports resolve
            checkPackageNames(javaClass, packages);

            // Check all types used are properly imported
            checkTypesAreImported(javaClass, packages);

            // Deal with variable redeclaration within scopes
            checkVariableDeclarationScopes(javaClass.ast);
        }
    }
}