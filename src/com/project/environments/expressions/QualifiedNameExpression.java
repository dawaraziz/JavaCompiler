package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;

import java.util.ArrayList;

public class QualifiedNameExpression extends Expression {
    ArrayList<Expression> names = new ArrayList<>();
    Expression currExpr;

    public QualifiedNameExpression(final ASTHead head, final Scope parentScope) {

        names.add(new NameExpression(head.getChild(head.getChildren().size()-1), this));
        int j = 0;

        for (int i = head.getChildren().size()-2; i >= 0; --i) {
            if ((i % 2) == 1) continue;

            this.currExpr = new NameExpression(head.getChild(i), names.get(j));
            j += 1;

            names.add(this.currExpr);
        }

    }

    @Override
    public boolean isVariableNameUsed(String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {

    }

    @Override
    public void checkTypeSoundness() {

    }
}
