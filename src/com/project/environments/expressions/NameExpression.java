package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.PackageScope;
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

            final StringBuilder fullName = new StringBuilder(nameList.get(0));

            disambiguateSimpleName(fullName.toString(), ast.getLeftmostChild());

            for (int i = 1; i < nameList.size(); ++i) {
                final int index = ast.getChildren().size() - 1 - (i * 2);
                final int previousIndex = index + 2;

                disambiguateQualifiedName(fullName.toString(),
                        nameList.get(i),
                        ast.getChild(index),
                        ast.getChild(previousIndex).getKind());

                fullName.append(nameList.get(i));
            }
        }
    }

    /**
     * https://web.archive.org/web/20120105104400/http://java.sun.com/docs/books/jls/second_edition/html/names.doc.html#44352
     */
    private void disambiguateSimpleName(final String ambiguousName, final ASTHead nodeHead) {

        // Check if we can find the name as a field, parameter, or local definition.
        if (getParentClass().checkIdentifierAgainstFields(ambiguousName)
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

    private void disambiguateQualifiedName(final String previousName,
                                           final String ambiguousName,
                                           final ASTHead nodeHead,
                                           final Kind previousKind) {
        // Need to take entire name on the left.
        if (previousKind == Kind.PACKAGENAME) {
            final PackageScope packageScope = getParentClass().packageMap.get(previousName);

            if (packageScope != null
                    && packageScope.classes.stream().anyMatch(c -> c.name.equals(ambiguousName))) {
                nodeHead.classifyAsTypeName();
            } else {
                nodeHead.classifyAsPackageName();
            }
        } else if (previousKind == Kind.TYPENAME) {
            final ClassScope matchingClass = getParentClass().findClass(previousName);

            if (matchingClass == null) {
                System.err.println("Could not find typename for ambiguous type name.");
                System.exit(42);
            }

            if (matchingClass.checkIdentifierAgainstFields(ambiguousName)
                    || matchingClass.checkIdentifierAgainstMethods(ambiguousName)) {
                nodeHead.classifyAsExpressionName();
            } else {
                System.err.println("Could not reclassify ambiguous typename.");
                System.exit(42);
            }
        } else if (previousKind == Kind.EXPRESSIONNAME) {
            final ClassScope matchingClass = getParentClass().findClass(previousName);

            if (matchingClass == null) {
                System.err.println("Could not find typename for ambiguous expression name.");
                System.exit(42);
            }

            if (matchingClass.checkIdentifierAgainstFields(ambiguousName)
                    || matchingClass.checkIdentifierAgainstMethods(ambiguousName)) {
                nodeHead.classifyAsExpressionName();
            } else {
                System.err.println("Could not reclassify ambiguous expression name.");
                System.exit(42);
            }
        } else {
            System.err.println("Ambiguous name type is wrong; aborting!");
            System.exit(42);
        }
    }

    @Override
    public void checkTypeSoundness() {
    }

    boolean isExpressionName() {
        return ast.isExpressionName();
    }
}
