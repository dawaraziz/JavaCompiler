package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;

import static com.project.scanner.structure.Kind.*;

public class QualifiedNameExpression extends Expression {
    ArrayList<Expression> names = new ArrayList<>();
    Expression currExpr;

    public QualifiedNameExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;

        names.add(new NameExpression(head.getChild(head.getChildren().size()-1), this));
        int j = 0;

        for (int i = head.getChildren().size()-2; i >= 0; --i) {
            if ((i % 2) == 1) continue;

            this.currExpr = new NameExpression(head.getChild(i), names.get(j));
            j += 1;

            names.add(this.currExpr);
        }

    }

    private void resolveLeftMostAmbiguousName() {

        for (Expression expression : names) {
            String nameLexeme = expression.name;
            Kind nameKind = expression.ast.getKind();
            if (nameKind != AMBIGUOUSNAME) return;

            // Check if we can find the name as a field, parameter, or local definition.
            if (getParentClass().checkIdentifierAgainstFields(nameLexeme)
                    || getParentMethod().checkIdentifierAgainstParameters(nameLexeme)
                    || getParentLocalDefinitions().stream().anyMatch(c -> c.checkIdentifier(nameLexeme))) {
                nameKind = EXPRESSIONNAME;

                // Check if our parent class either is or single imports the name.
            } else if (getParentClass().checkIdentifier(nameLexeme)
                    || getParentClass().checkIdentifierAgainstSingleImports(nameLexeme)) {
                nameKind = TYPENAME;
            }

            // Check if we can find the name in our package.
            else if (getParentClass().checkIdentifierAgainstPackageImports(nameLexeme)) {
                nameKind = TYPENAME;
            }

            // Check if the name appears one on-demand import.
            // If it appears in more than one, this exits as an error.
            else if (getParentClass().checkIdentifierAgainstOnDemandImports(nameLexeme)) {
                nameKind = TYPENAME;
            }

            // Otherwise, it's a PackageName.
            else {
                nameKind = PACKAGENAME;
            }
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
