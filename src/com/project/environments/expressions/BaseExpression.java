package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

public class BaseExpression extends Expression {
    final Expression LHS;
    final Expression singular;
    final Expression RHS;

    public BaseExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        if (head.getChildren().size() == 3
                && head.getChild(0).getKind() == Kind.PAREN_CLOSE
                && head.getChild(2).getKind() == Kind.PAREN_OPEN) {
            LHS = null;
            singular = generateExpressionScope(head.getChild(1), this);
            RHS = null;
        } else if (head.getChildren().size() == 2) {
            LHS = null;
            singular = generateExpressionScope(head.getChild(0), this);
            RHS = null;
        } else {
            LHS = generateExpressionScope(head.getChild(2), this);
            singular = null;
            RHS = generateExpressionScope(head.getChild(0), this);
        }
    }

    @Override
    public Kind evaluatesTo(){
        return booleanOrKind(Kind.BOOLEAN);
    }

    @Override
    public boolean isVariableNameUsed(String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {
        if (LHS != null) {
            LHS.linkTypesToQualifiedNames(rootClass);
            RHS.linkTypesToQualifiedNames(rootClass);

            switch (this.ast.getLexeme()) {
                case "RELATIONALEXPRESSION":
                case "EQUALITYEXPRESSION":
                case "ANDEXPRESSION":
                case "EXCLUSIVEOREXPRESSION":
                case "CONDITIONALEXPRESSION":
                case "CONDITIONALOREXPRESSION":
                case "INCLUSIVEOREXPRESSION":
                case "CONDITIONALANDEXPRESSION":
                    this.type = new Type(Type.PRIM_TYPE.BOOLEAN);
                    break;
                case "MULTIPLICATIVEEXPRESSION":
                    this.type = new Type(Type.PRIM_TYPE.INT);
                    break;
                case "SUBBASEEXPRESSION":
                    this.type = RHS.type;
                    break;
                default:
                    System.err.println("Could not type Base Expr!");
                    System.exit(42);
            }
        } else {
            singular.linkTypesToQualifiedNames(rootClass);
            this.type = singular.type;
        }

        if (this.type == null) {
            System.err.println("Could not type Base Expr after!");
            System.exit(42);
        }
    }

    @Override
    public void checkTypeSoundness() {
        if (LHS != null) {
            LHS.checkTypeSoundness();
            RHS.checkTypeSoundness();

            if (this.ast.getChild(1).getLexeme().equals("COMMA")) {
                System.err.println("Found comma in BaseExpression.");
                System.exit(42);
            }

            if (LHS.type.equals(RHS.type)) return;

            if (RHS.type.equals(Type.generateObjectType()) && LHS.type.isArray) {

            } else if ((LHS.type.isReferenceType() && RHS.type.isNullType())
                    || (RHS.type.isReferenceType() && LHS.type.isNullType())) {

            } else if (LHS.type.isReferenceType() && RHS.type.isReferenceType()) {
                final ClassScope parentClass = this.getParentClass();
                final ClassScope LHSClass = parentClass.classMap.get(LHS.type.name.getDefaultlessQualifiedName());
                final ClassScope RHSClass = parentClass.classMap.get(RHS.type.name.getDefaultlessQualifiedName());

                if (!RHSClass.isSubClassOf(LHSClass) && !LHSClass.isSubClassOf(RHSClass)) {
                    System.err.println("Unsound type: Base Expression, differing types");
                    System.exit(42);
                }
            } else {
                System.err.println("Unsound type: Base Expression, differing types");
                System.exit(42);
            }
        }
    }
}