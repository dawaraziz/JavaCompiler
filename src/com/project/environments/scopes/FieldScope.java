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
        if (initializer != null) initializer.linkTypesToQualifiedNames(rootClass);
        checkFieldDeclarations(rootClass);
    }

    // Make sure field declarations legally use types
    public void checkFieldDeclarations(final ClassScope rootClass) {
        // Get lhs used variables
        // Find kind of EQUAL and then get any of kind EXPRESSIONNAME that are its children
        ArrayList<ASTNode> names = this.ast.getRHSExpressionNames();
        ArrayList<ASTNode> culledNames = new ArrayList<>();

        if (names != null){

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

            // Create a hashset of field variables already declared
            HashSet<String> seenDeclarations = new HashSet<>();
            for (int i = rootClass.fieldTable.size()-1; i >=0; i-- ){
                FieldScope fscope = rootClass.fieldTable.get(i);
                if (fscope == this){
                    break;
                }
                seenDeclarations.add(fscope.name);
            }

            for(ASTNode name : culledNames){
                // If name wasn't previously declared as a field or it is the variable that is currently being declared fail
                if (name.lexeme.equals(this.name) || !seenDeclarations.contains(name.lexeme)){
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
                if (!n.parentOneOfLexeme("QUALIFIEDNAME", "FIELDACCESS", "ASSIGNMENT")) {
                    culledNames.add(n);
                }
                if (n.parentOneOfLexeme("ASSIGNMENT") && n.parent != lastParent) {
                    ArrayList<ASTNode> RHSNames = n.parent.findChildKindsAfterNodeWithLexeme(Kind.EXPRESSIONNAME, "ASSIGNMENTOPERATOR");
                    ArrayList<ASTNode> recurse = new ArrayList<>();
                    for (ASTNode node : RHSNames){
                        if(!node.parentOneOfLexeme("QUALIFIEDNAME", "FIELDACCESS", "ASSIGNMENT")) {
                            culledNames.add(n);
                        }
                        else{
                            recurse.add(n);
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

    boolean checkIdentifier(final String identifier) {
        return identifier.equals(name);
    }

    @Override
    public ArrayList<String> generatei386Code() {
        return new ArrayList<>();
    }

    public String generateStaticFieldCode() {
        if (!modifiers.contains("static")) {
            System.err.println("Non-static field cannot generate static code; aborting!");
            System.exit(42);
        }

        return "common " + callStaticLabel() + " 4";
    }

    public String generateStaticFieldExtern() {
        if (!modifiers.contains("static")) {
            System.err.println("Non-static field cannot generate static code; aborting!");
            System.exit(42);
        }

        return "extern " + callStaticLabel();
    }

    public ArrayList<String> generateStaticInitializationCode() {
        if (!modifiers.contains("static")) {
            System.err.println("Non-static field cannot generate static code; aborting!");
            System.exit(42);
        }

        final ArrayList<String> code = new ArrayList<>();
        if (initializer == null) {
            code.add("mov dword [" + callStaticLabel() + "], 0");
        } else {
            code.addAll(initializer.generatei386Code());
            code.add("mov [" + callStaticLabel() + "], eax");
        }

        return code;
    }

    public String callStaticLabel() {
        if (!modifiers.contains("static")) {
            System.err.println("Non-static field cannot generate static code; aborting!");
            System.exit(42);
        }

        return ((ClassScope) parentScope).generateClassLabel() + "_static_" + name;
    }

}
