package com.project.environments.statements;

import com.project.environments.ast.ASTHead;
import com.project.environments.expressions.Expression;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.MethodScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;

import java.util.ArrayList;
import java.util.HashMap;

import static com.project.environments.expressions.Expression.generateExpressionScope;
import static com.project.environments.structure.Type.PRIM_TYPE.BYTE;
import static com.project.environments.structure.Type.PRIM_TYPE.CHAR;
import static com.project.environments.structure.Type.PRIM_TYPE.INT;
import static com.project.environments.structure.Type.PRIM_TYPE.SHORT;
import static com.project.environments.structure.Type.PRIM_TYPE.VAR;
import static com.project.environments.structure.Type.PRIM_TYPE.VOID;

public class ReturnStatement extends Statement {
    private final Expression expression;

    @Override
    public void checkReturnedTypes(final Type type, final HashMap<String, ClassScope> classmap) {
        if (expression == null) {
            if (type.prim_type != VOID) {
                System.err.println("Returned nothing but Method was not of type void");
                System.exit(42);
            }
        } else if (expression.type == null) {
        } else if (type.prim_type == VOID) {
            System.err.println("Encountered non-void return in void method.");
            System.exit(42);
        } else if ((expression.type.prim_type == VAR
                && type.prim_type == VAR
                && !(expression.type.name == null)
                && (expression.type.name.getQualifiedName().equals("null")))
                || implicitUpcast(type, expression.type)
                || objectUpcast(classmap, type, expression.type)) {
        } else if (!expression.type.equals(type)) {
            System.err.println("Encountered return with incorrect type.");
            System.exit(42);
        }
    }

    private boolean implicitUpcast(final Type decRetType, final Type retType) {
        if (decRetType.prim_type == INT) {
            if (retType.prim_type == INT ||
                    retType.prim_type == CHAR ||
                    retType.prim_type == BYTE ||
                    retType.prim_type == SHORT
            ) {
                return decRetType.isArray == retType.isArray;
            }
        }
        return false;
    }


    private boolean objectUpcast(final HashMap<String, ClassScope> classmap, final Type decRetType, final Type retType) {
        if (decRetType.prim_type == VAR && retType.prim_type == VAR) {
            final String decName = decRetType.name.getQualifiedName();
            final String retName = retType.name.getQualifiedName();
            final ClassScope decScope = classmap.get(decName);
            final ClassScope retScope = classmap.get(retName);
            if (retScope != null && decScope != null) {
                if (retScope.isSubClassOf(decScope)) {
                    return decRetType.isArray == retType.isArray;
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

    ReturnStatement(final ASTHead head, final Scope parentScope) {
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
    }

    @Override
    public ArrayList<String> generatei386Code() {
        final ArrayList<String> code = new ArrayList<>();

        if (expression != null) {
            code.addAll(expression.generatei386Code());
        }

        if (getParentMethod() != null) {
            code.add("jmp " + getParentMethod().callEndLabel());
        } else if (getParentConstructor() != null) {
            code.add("jmp " + getParentConstructor().callEndLabel());
        } else {
            System.err.println("Found definition outside method or constructor?");
            System.exit(42);
        }

        return code;
    }
}
