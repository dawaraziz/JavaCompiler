package com.project.type_linker;

import com.project.environments.ClassScope;
import com.project.environments.ImportScope;
import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;
import com.project.parser.structure.ParserSymbol;
import com.project.scanner.structure.Kind;
import com.sun.corba.se.spi.ior.IdentifiableFactory;
import resources.Pair;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Stack;
import java.util.stream.Collectors;

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
        if (parentIsLexeme(node, "TYPEIMPORTONDEMANDDECLARATION")){
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
                if (within(node, "CLASSBODY")){
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
//                System.out.println("New Constructor or method Scope");
            }
            // New Scope add a new array to scope stack
            else if (curr.kind == Kind.CURLY_BRACKET_OPEN) {
                currentScope.push(new ArrayList<>());
//                System.out.println("Push Scope");
            }
            // Moved up a scope pop off scopeStack
            else if (curr.kind == Kind.CURLY_BRACKET_CLOSE){
                if (curr.parent.lexeme.equals("CONSTRUCTORBODY") || curr.parent.parent.lexeme.equals("METHODDECLARATION")){
                    scopesStack.pop();
//                    System.out.println("Pop Method or Constructor Scope");
                }
                else {
                    currentScope.pop();
//                    System.out.println("Pop Scope");
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

    public static void link(final ArrayList<ClassScope> classTable, final HashMap<String, ClassScope> classMap){
        for (ClassScope javaClass : classTable) {

            // Check no import clashes with class or interface definitions
            for (ImportScope imp : javaClass.imports) {
                System.out.println(imp.name.getClassName() + " = " + javaClass.name);
                if (imp.name.getClassName().equals(javaClass.name)){
                    System.err.println("Class import same as class or interface declared");
                    System.exit(42);
                }
            }

            //Check if two single-type-import declarations
            for (int i = 0; i < javaClass.imports.size(); i++) {
                for (int j = i+1; j < javaClass.imports.size(); j++) {
                    ImportScope import1 = javaClass.imports.get(i);
                    ImportScope import2 = javaClass.imports.get(j);
                    System.out.println(import1.name.getClassName() + " = " + import2.name.getClassName());
                    if (import1.name.getClassName().equals(import2.name.getClassName())) {
                        System.err.println("Two single-type imports clashed");
                        System.exit(42);
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
                System.out.println("Does it contain " + name);
                boolean found = false;
                for (int i = 0; i < classTable.size(); ++i){
                    System.out.println("Checking " + classTable.get(i).name);
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


            // Deal with variable redeclaration within scopes
            checkVariableDeclarationScopes(astHead);





        }
        return;
    }
}




