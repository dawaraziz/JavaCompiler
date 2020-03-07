package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Name;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;

import static com.project.environments.ast.ASTNode.lexemesToStringList;
import static java.util.Collections.reverse;

public class NameExpression extends Expression {
    final Name nameClass;

    public NameExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        nameClass = new Name(lexemesToStringList(head.unsafeGetHeadNode().getLeafNodes()));
    }

    @Override
    public boolean isVariableNameFree(final String variableName) {
        return parentScope.isVariableNameFree(variableName);
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {

        // First, let's try to disambiguate any AmbiguousNames.
        if (nameClass.isSimpleName()) {
            disambiguateSimpleName(nameClass.getSimpleName(), ast);
        } else {
            final ArrayList<String> nameList = nameClass.getNameList();
            reverse(nameList);

            disambiguateSimpleName(nameList.get(0), ast.getLeftmostChild());

            for (int i = 1; i < nameList.size(); ++i) {
                final int index = ast.getChildren().size() - 1 - (i * 2);
                final int previousIndex = index + 2;

                disambiguateQualifiedName(nameList.get(i),
                        ast.getChild(index),
                        ast.getChild(previousIndex).getKind());
            }
        }
    }

    /**
     * https://web.archive.org/web/20120105104400/http://java.sun.com/docs/books/jls/second_edition/html/names.doc.html#44352
     */
    private void disambiguateSimpleName(final String ambiguousName, final ASTHead nodeHead) {

        // Check if we can find the name as a field, parameter, or local definition.
        if (getParentFields().stream().anyMatch(c -> c.checkIdentifier(ambiguousName))
                || getParentMethod().checkIdentifierAgainstParameters(ambiguousName)
                || getParentLocalDefinitions().stream().anyMatch(c -> c.checkIdentifier(ambiguousName))) {
            nodeHead.classifyAsExpressionName();

            // Check if our parent class either is or single imports the name.
        } else if (getParentClass().checkIdentifier(ambiguousName)
                || getParentClass().checkIdentifierAgainstSingleImports(ambiguousName)) {
            nodeHead.classifyAsTypeName();
        }

        // Check if we can find the name in our package.
        else if (getParentClass().checkIdentifierAgainstPackageImports(ambiguousName)) {
            nodeHead.classifyAsTypeName();
        }

        // Check if the name appears one on-demand import.
        // If it appears in more than one, this exits as an error.
        else if (getParentClass().checkIdentifierAgainstOnDemandImports(ambiguousName)) {
            nodeHead.classifyAsTypeName();
        }

        // Otherwise, it's a PackageName.
        else {
            nodeHead.classifyAsPackageName();
        }
    }

    private void disambiguateQualifiedName(final String ambiguousName,
                                           final ASTHead nodeHead,
                                           final Kind previousKind) {
        // Need to take entire name on the left.
        if (previousKind == Kind.PACKAGENAME) {

        } else if (previousKind == Kind.TYPENAME) {

        } else if (previousKind == Kind.EXPRESSIONNAME) {

        }
        // TODO:
    }

    @Override
    public void checkTypeSoundness() {
    }

    boolean isExpressionName() {
        return ast.isExpressionName();
    }
}
