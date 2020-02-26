package com.project.type_linker;

import com.project.environments.ClassScope;
import com.project.environments.ImportScope;
import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;
import com.project.parser.structure.ParserSymbol;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;
import java.util.HashMap;

public class TypeLinker {
    public static void disambiguate(final ASTHead astHead){
        // Get every Variable_ID and classify it as
        // PackageName, TypeName, ExpressionName, MethodName, PackageOrTypeName, or AmbiguousName
        ArrayList<ASTNode> nameNodes = astHead.unsafeGetHeadNode().findNodesWithKinds(Kind.VARIABLE_ID);
        for (ASTNode node : nameNodes){
            // Check if TypeName
            changeIfTypeName(node);
            changeIfExpressionName(node);
            changeIfMethodName(node);
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

    public static void changeIfTypeName(ASTNode node){

        // In a single-type-import declaration (§7.5.1)
        if (node.parent.parent != null && node.parent.parent.lexeme.equals("SINGLETYPEIMPORTDECLARATION")) {
            if (node.parent != null && (node.parent.lexeme.equals("QUALIFIEDNAME"))) {
                // If it's the end of the qualified name it is a methodname
                ASTNode last = node.parent.children.get(0);
                if (node == last) {
                    node.kind = Kind.TYPENAME;
                }
            }
        }

        if (parentIsLexeme(node, "SINGLETYPEIMPORTDECLARATION")){
            node.kind = Kind.TYPENAME;
        }

        // In an extends clause in a class declaration (§8.1.3)
        if (parentIsLexeme(node, "SUPER")){
            node.kind = Kind.TYPENAME;
        }

        // In an implements clause in a class declaration (§8.1.4)
        if (parentIsLexeme(node, "INTERFACES")){
            node.kind = Kind.TYPENAME;
        }

        // In an extends clause in an interface declaration (§9.1.2)
        if (parentIsLexeme(node, "EXTENDSINTERFACES")){
            node.kind = Kind.TYPENAME;
        }

        if (parentIsLexeme(node, "INTERFACEDECLARATION")){
            node.kind = Kind.TYPENAME;
        }

        // In a field declaration
        if (parentIsLexeme(node, "FIELDDECLARATION")){
            node.kind = Kind.TYPENAME;
        }

        // As the result type of a method
        if (parentIsLexeme(node, "METHODHEADER")){
            node.kind = Kind.TYPENAME;
        }

        // As the type of a formal parameter of a method or constructor
        if (parentIsLexeme(node, "FORMALPARAMETER")){
            node.kind = Kind.TYPENAME;
        }

        if (parentIsLexeme(node, "CONSTRUCTORDECLARATOR")){
            node.kind = Kind.TYPENAME;
        }

        // As the type of an exception that can be thrown by a method or constructor - IRRELEVANT TO JOOS

        // As the type of a local variable (§14.4)
        if (parentIsLexeme(node, "LOCALVARIABLEDECLARATION")){
            node.kind = Kind.TYPENAME;
        }

        // As the type of an exception parameter in a catch clause of a try statement (§14.19) - IRRELEVANT TO JOOS

        // As the type in a class literal (§15.8.2) - A class literal is an expression consisting of the name
        // of a class, interface, array, or primitive type followed by a `.' and the token class
        // ArrayList.add() not sure how to do this one, i need to know ArrayList is one of these


        // As the qualifying type of a qualified this expression (§15.8.4). - idk what this means


        // As the class type which is to be instantiated in an unqualified class instance creation expression (§15.9)
        // i.e new Arraylist()
        if (parentIsLexeme(node, "CLASSINSTANCECREATIONEXPRESSION")){
            node.kind = Kind.TYPENAME;
        }


        // As the direct superclass or direct superinterface of an anonymous class (§15.9.5) which is to be instantiated in an unqualified class instance creation expression (§15.9)
        // As the element type of an array to be created in an array creation expression (§15.10)
        // As the qualifying type of field access using the keyword super (§15.11.2)
        // As the qualifying type of a method invocation using the keyword super (§15.12)
        // As the type mentioned in the cast operator of a cast expression (§15.16)


        // As the type that follows the instanceof relational operator (§15.20.2)

    }

    public static void changeIfExpressionName(ASTNode node) {
        // As the qualifying expression in a qualified superclass constructor invocation (§8.8.5.1) - IRRELEVANT TO JOOS
        // ex - Primary.super ( ArgumentListopt ) ;

        // As the qualifying expression in a qualified class instance creation expression (§15.9) - IRRELEVANT TO JOOS
        // ex - Primary.new Identifier ( ArgumentListopt ) ClassBodyopt

        // As a PostfixExpression (§15.14) - IRRELEVANT TO JOOS

        if (node.kind.equals(Kind.VARIABLE_ID)) {

            // As the array reference expression in an array access expression (§15.13)
            if (parentIsLexeme(node, "ARRAYACCESS")){
                node.kind = Kind.EXPRESSIONNAME;
            }

            // As the left-hand operand of an assignment operator (§15.26)
            else if (parentIsLexeme(node, "VARIABLEDECLARATORID")){
                node.kind = Kind.EXPRESSIONNAME;
            }
        }
    }

    public static void changeIfMethodName(ASTNode node) {
        // Before the "(" in a method invocation expression
        if (node.kind.equals(Kind.VARIABLE_ID)) {
            if (parentIsLexeme(node, "METHODINVOCATION")){
                node.kind = Kind.EXPRESSIONNAME;
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
                node.kind = Kind.EXPRESSIONNAME;
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

        // In a type-import-on-demand declaration (§7.5.2)
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



        }
        return;
    }
}
