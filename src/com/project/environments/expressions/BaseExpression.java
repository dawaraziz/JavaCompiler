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
    public boolean isVariableNameUsed(String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {
        if (LHS != null) {
            LHS.linkTypesToQualifiedNames(rootClass);
            RHS.linkTypesToQualifiedNames(rootClass);
            String exprType = this.ast.getLexeme();
            switch (exprType) {
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
                case "ASSIGNMENT":
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
    }

    @Override
    public void checkTypeSoundness() {
        if (LHS != null) {
            LHS.checkTypeSoundness();
            RHS.checkTypeSoundness();
            if (!this.ast.getChild(1).getLexeme().equals("COMMA")) {
                if (!LHS.type.equals(RHS.type)) {
                    // TODO: Possibly need to deal with case when we have INT and INTEGER_LITERAL etc

                    System.err.println("Unsound type: Base Expression, differing types");
                    System.exit(42);
                }
            }
        }
    }
}
