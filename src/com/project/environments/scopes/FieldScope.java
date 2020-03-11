package com.project.environments.scopes;

import com.project.environments.ast.ASTHead;
import com.project.environments.ast.ASTNode;
import com.project.environments.expressions.Expression;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;
import java.util.HashSet;

public class FieldScope extends Scope {
    public final ArrayList<String> modifiers;
    public final Expression initializer;

    FieldScope(final ASTHead head, final ClassScope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = ast.getFieldName();
        System.out.println("Name is: " + this.name);
        this.type = ast.getFieldType();
        this.modifiers = head.getFieldModifiers();
        this.initializer = Expression.generateExpressionScope(ast.getFieldInitializer(), this);
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        type.linkType(rootClass);
        checkFieldDeclarations(rootClass);
    }

    // Make sure field declarations legally use types
    public void checkFieldDeclarations(final ClassScope rootClass) {
        // Get lhs used variables
        // Find kind of EQUAL and then get any of kind EXPRESSIONNAME that are its children
        ArrayList<ASTNode> names = this.ast.getRHSExpressionNames();
        ArrayList<ASTNode> culledNames = new ArrayList<>();

        if (names != null){
//            culledNames = recursiveCulling(names, null);
            System.out.println("LENGTH: " + culledNames);

            // Remove any expressions used as qualified names, field access, or Assignments
            for (ASTNode n : names){
                if (!n.parentOneOfLexeme("QUALIFIEDNAME", "FIELDACCESS", "ASSIGNMENT")){
                    culledNames.add(n);
                }
                // add the RHS of any assignment within the RHS of the declaration
                if (n.parentOneOfLexeme("ASSIGNMENT")){
                    ArrayList<ASTNode> RHSNames = n.parent.findChildKindsAfterNodeWithLexeme(Kind.EXPRESSIONNAME, "ASSIGNMENTOPERATOR");
                    for (ASTNode node : RHSNames){
                        if(!node.parentOneOfLexeme("QUALIFIEDNAME", "FIELDACCESS")) {
                            culledNames.add(n);
                        }
                    }

                }
            }

            System.out.println("CHECK FSCOPE DECLARATION: "+ this.name);

            // Create a hashset of field variables already declared
            HashSet<String> seenDeclarations = new HashSet<>();
            for (int i = rootClass.fieldTable.size()-1; i >=0; i-- ){
                FieldScope fscope = rootClass.fieldTable.get(i);
                System.out.println("seeing: "+ fscope.name);
                if (fscope == this){
                    break;
                }
                System.out.println("add: "+ fscope.name);
                seenDeclarations.add(fscope.name);
            }

            for(ASTNode name : culledNames){
                System.out.println("check: "+ name.lexeme);
                // If name wasn't previously declared as a field or it is the variable that is currently being declared fail
                if (name.lexeme.equals(this.name) && !seenDeclarations.contains(name.lexeme)){
                        System.err.println("Expression Name " + name.lexeme + " used in RHS of field declaration that is declared as another field variable in " + rootClass.name);
                        System.exit(42);
                }
            }
        }
    }

    public ArrayList<ASTNode> recursiveCulling(ArrayList<ASTNode> names, ASTNode lastParent){
        ArrayList<ASTNode> culledNames = new ArrayList<>();
        if (names != null) {
            // Remove any expressions used as qualified names, field access, or Assignments
            for (ASTNode n : names) {
                System.out.println("COMON " + n.lexeme + " : " + n.parent.lexeme);
                if (!n.parentOneOfLexeme("QUALIFIEDNAME", "FIELDACCESS", "ASSIGNMENT")) {
                    System.out.println("ADD IT");
                    culledNames.add(n);
                }
                if (n.parentOneOfLexeme("ASSIGNMENT") && n.parent != lastParent) {
                    System.out.println("forever: " + n.lexeme);
                    ArrayList<ASTNode> RHSNames = n.parent.findChildKindsAfterNodeWithLexeme(Kind.EXPRESSIONNAME, "ASSIGNMENTOPERATOR");
                    ArrayList<ASTNode> recurse = new ArrayList<>();
                    for (ASTNode node : RHSNames){
                        if(!node.parentOneOfLexeme("QUALIFIEDNAME", "FIELDACCESS", "ASSIGNMENT")) {
                            culledNames.add(n);
                            System.out.println("add to RHS cull: " + n.lexeme);
                        }
                        else{
                            recurse.add(n);
                            System.out.println("add to recurse: " + n.lexeme);
                        }
                    }
                    culledNames.addAll(recursiveCulling(recurse, n.parent));
                }
            }
        }
        return culledNames;
    }

    @Override
    public void checkTypeSoundness() {
        // TODO: Uncomment when expression types are implemented.
//        if (!type.equals(initializer.type)) {
//            System.err.println("Field initializer has wrong type.");
//            System.exit(42);
//        }
    }

    public boolean checkIdentifier(final String identifier) {
        return identifier.equals(name);
    }
}
