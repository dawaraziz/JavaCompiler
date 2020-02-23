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
            // Check if MethodName
            // I want to go to parent and then see if in its children there is a Paren_open
            // but the parent could be a Qualified Name, or one of many things, also the order matters
            // the parenthesis would need to be directly after

            // Check if TypeName
            changeIfTypeName(node);
        }
    }

    public static void changeIfTypeName(ASTNode node){
        // In a field declaration
        if (node.parent != null && node.parent.lexeme.equals("FIELDDECLARATION")){
            System.out.println("result of a field dec : " + node.lexeme);
            node.kind = Kind.TYPENAME;
        }

        // As the result type of a method
        if (node.parent != null && node.parent.lexeme.equals("METHODHEADER")){
            System.out.println("result of method : " + node.lexeme);
            node.kind = Kind.TYPENAME;
        }

        // As the type of a formal parameter of a method or constructor
        if (node.parent != null && node.parent.lexeme.equals("FORMALPARAMETER")){
            System.out.println("result of method : " + node.lexeme);
            node.kind = Kind.TYPENAME;
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



        }
        return;
    }
}
