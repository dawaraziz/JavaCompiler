package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.MethodScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Name;
import com.project.environments.structure.Type;

import java.util.HashMap;

import static com.project.environments.expressions.Expression.generateExpressionScope;
import static com.project.environments.structure.Type.PRIM_TYPE.*;

public class ReturnStatement extends Statement {
    final Expression expression;

    @Override
    public void checkReturnedTypes(Type type, HashMap<String, ClassScope> classmap) {
//        System.out.println(type + " : " + expression.type);
//         Return nothing i.e return;
        if (expression == null){
            if (type.prim_type != VOID){
                System.err.println("Returned nothing but Method was not of type void");
                System.exit(42);
            }
        }
        //TODO: FIX THIS SEEMS TO OCCUR IN A RETURN IN CONSTRUCTOR, but we need to check these!
        else if (expression.type == null){
            System.out.println("There is no information on the return type");
            return;
        }
        else if (type.prim_type == VOID && expression != null) {
            System.err.println("Encountered non-void return in void method.");
            System.exit(42);
        }
        else if (expression.type.prim_type == VAR && type.prim_type == VAR && !(expression.type.name == null) && (expression.type.name.getQualifiedName().equals("null"))) {
            return;
        }
        else if (implicitUpcast(type, expression.type)) {
            return;
        }
        // Can return java.lang.String to java.lang.Object
        else if (objectUpcast(classmap, type, expression.type)) {
            return;
        }
        // default
        else if (!expression.type.equals(type)) {
            System.err.println("Encountered return with incorrect type.");
            System.exit(42);
        }
        return;
    }

    public boolean implicitUpcast(Type decRetType, Type retType) {
        if(decRetType.prim_type == INT){
            if (retType.prim_type == INT ||
                    retType.prim_type == CHAR ||
                    retType.prim_type == BYTE ||
                    retType.prim_type == SHORT
            ){
                if (decRetType.isArray == retType.isArray){
                    return true;
                }
            }
        }
        return false;
    }


    public boolean objectUpcast(HashMap<String, ClassScope> classmap, Type decRetType, Type retType) {
        if(decRetType.prim_type == VAR && retType.prim_type == VAR){
            String decName = decRetType.name.getQualifiedName();
            String retName = retType.name.getQualifiedName();
            ClassScope decScope = classmap.get(decName);
            ClassScope retScope = classmap.get(retName);
            if(retScope != null && decScope != null) {
                if (retScope.isSubClassOf(decScope)) {
                    System.out.println(retScope.name + " is subclass$ of :" + decScope.name);
                    if (decRetType.isArray == retType.isArray) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public void assignReachability() {
        out = false;
    }

    @Override
    public void checkReachability() {
        if (!in) {
            System.err.println("Found unreachable return statement.");
            System.exit(42);
        }
    }

    public ReturnStatement(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        expression = (head.getChildren().size() == 2)
                ? null
                : generateExpressionScope(head.getChild(1), this);
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return parentScope.isVariableNameUsed(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        if (expression != null) expression.linkTypesToQualifiedNames(rootClass);
    }

    @Override
    public void checkTypeSoundness() {
        Scope parentMethod = parentScope;
        while (!(parentMethod instanceof MethodScope)) {
            parentMethod = parentMethod.parentScope;
        }

        // TODO: Uncomment when expression types are implemented.
//        if (parentMethod.type.prim_type == VOID && expression != null) {
//            System.err.println("Encountered non-void return in void method.");
//            System.exit(42);
//        } else if (expression == null || !expression.type.equals(parentMethod.type)) {
//            System.err.println("Encountered return with incorrect type.");
//            System.exit(42);
//        }
    }
}
