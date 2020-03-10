package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.FieldScope;
import com.project.environments.scopes.MethodScope;
import com.project.environments.scopes.PackageScope;
import com.project.environments.scopes.Scope;
import com.project.environments.statements.DefinitionStatement;
import com.project.environments.structure.Name;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

import static com.project.environments.scopes.ClassScope.CLASS_TYPE.CLASS;
import static com.project.environments.scopes.ClassScope.CLASS_TYPE.INTERFACE;
import static com.project.scanner.structure.Kind.AMBIGUOUSNAME;
import static com.project.scanner.structure.Kind.EXPRESSIONNAME;
import static com.project.scanner.structure.Kind.METHODNAME;
import static com.project.scanner.structure.Kind.PACKAGENAME;
import static com.project.scanner.structure.Kind.PACKAGEORTYPENAME;
import static com.project.scanner.structure.Kind.TYPENAME;

public class NameExpression extends Expression {
    private final String nameLexeme;
    private Kind nameKind;

    private final NameExpression qualifier;

    private Scope namePointer;

    NameExpression(final ASTHead head ,final NameExpression parentName) {
        this.qualifier = parentName;
        this.parentScope = parentName.parentScope;
        this.nameLexeme = head.getLexeme();
    }

    NameExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        if (ast.getChildren().size() == 0) {
            nameLexeme = head.getLexeme();
            nameKind = head.getKind();
            qualifier = null;
        } else if (ast.getChildren().size() == 1) {
            nameLexeme = head.getChild(0).getLexeme();
            nameKind = head.getChild(0).getKind();
            qualifier = null;
        } else {
            nameLexeme = head.getChild(0).getLexeme();
            nameKind = head.getChild(0).getKind();
            qualifier = new NameExpression(head.generateNameSubHead(), this);
        }
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        if (qualifier != null) qualifier.linkTypesToQualifiedNames(rootClass);

        // First, reclassify any ambiguous names.
        if (qualifier == null) {
            resolveLeftMostAmbiguousName();
        } else {
            disambiguateInternalQualifiedName();
        }

        // Then, reclassify any choice names.
        if (nameKind == PACKAGEORTYPENAME) {
            if (getParentClass().checkIdentifier(nameLexeme)) {
                nameKind = TYPENAME;
            } else {
                nameKind = PACKAGENAME;
            }
        }

        // Then, we try to classify and clarify what each name points to.
        if (qualifier == null) {
            classifySimpleName();
        } else {
            classifyQualifiedName();
        }

        if (namePointer == null && type == null && nameKind != PACKAGENAME) {
            System.err.println("Could not identify name pointer; aborting!");
            System.exit(42);
        }

        if (type == null && namePointer != null) type = namePointer.type;
    }

    private void classifySimpleName() {
        final ClassScope parentClass = getParentClass();

        if (nameKind == PACKAGENAME) {
            if (!parentClass.isNamePrefixOfPackage(nameLexeme)) {
                System.err.println("Found invalid package name.");
                System.exit(42);
            }
        } else if (nameKind == TYPENAME) {
            namePointer = parentClass.resolveSimpleTypeName(nameLexeme);
        } else if (nameKind == EXPRESSIONNAME) {

            // First, check if we can find it in our method declaration.
            if (getParentMethod() != null) {
                final DefinitionStatement statement = getDefinitionScope(nameLexeme);

                if (statement != null) {
                    namePointer = statement;
                    return;
                }

                final Parameter parameter = getParentMethod()
                        .getParameterFromIdentifier(nameLexeme);
                if (parameter != null) {
                    namePointer = parameter;
                    return;
                }
            }

            // Then check if we can find it in our class declaration.
            final FieldScope fieldScope = parentClass.getIdentifierFromFields(nameLexeme);
            if (fieldScope != null) {
                namePointer = fieldScope;
            }
        } else if (nameKind == METHODNAME) {
            System.err.println("Method name resolution not implemented!");
            System.exit(42);
        }
    }

    private void classifyQualifiedName() {

        final ClassScope parentClass = getParentClass();
        final String qualifierName = getQualifierName();

        if (nameKind == PACKAGENAME) {
            if (!parentClass.isNamePrefixOfPackage(getQualifiedName())) {
                System.err.println("Found non-prefix package name.");
                System.exit(42);
            }
        } else if (nameKind == TYPENAME) {
            if (qualifier.nameKind == TYPENAME) {
                System.err.println("Found type qualified typename. JOOS ILLEGAL!");
                System.exit(42);
            } else if (qualifier.nameKind == PACKAGENAME) {
                final ClassScope classScope = parentClass.getClassFromPackage(qualifierName, nameLexeme);
                if (classScope == null) {
                    System.err.println("Could not identify package qualified type name");
                    System.exit(42);
                }
                namePointer = classScope;
            } else {
                System.err.println("Found non package qualified typename.");
                System.exit(42);
            }
        } else if (nameKind == EXPRESSIONNAME) {
            if (qualifier.nameKind == PACKAGENAME) {
                System.err.println("Found package name qualified expression name.");
                System.exit(42);
            } else if (qualifier.nameKind == TYPENAME) {
                final ClassScope qualifyingClass = getResolvedType();

                if (qualifyingClass.classType == CLASS) {
                    final FieldScope fieldScope = qualifyingClass.getIdentifierFromFields(nameLexeme);

                    if (fieldScope == null) {
                        System.err.println("Found type name qualified expression name with no field.");
                        System.exit(42);
                    }

                    namePointer = fieldScope;
                } else if (qualifyingClass.classType == INTERFACE) {
                    final FieldScope fieldScope = qualifyingClass.getIdentifierFromFields(nameLexeme);

                    if (fieldScope == null) {
                        System.err.println("Found type name qualified expression name with no field.");
                        System.exit(42);
                    }

                    namePointer = fieldScope;
                }
            } else if (qualifier.nameKind == EXPRESSIONNAME) {
                if (!qualifier.type.isReferenceType()) {
                    System.err.println("Found prim type as qualifier.");
                    System.exit(42);
                }

                // Arrays have a special field called length.
                if (qualifier.type.isArray && nameLexeme.equals("length")) {
                    type = new Type(Type.PRIM_TYPE.INT);
                    return;
                }

                final FieldScope fieldScope = getResolvedType().getIdentifierFromFields(nameLexeme);

                if (fieldScope == null) {
                    System.err.println("Found no field to type qualified expression name.");
                    System.exit(42);
                }

                namePointer = fieldScope;
            }
        } else if (nameKind == METHODNAME) {
            System.err.println("Method name resolution not implemented!");
            System.exit(42);

//            if (qualifier.nameKind == PACKAGENAME) {
//                System.err.println("Found package name qualified method name.");
//                System.exit(42);
//            } else {
//                final MethodScope scope = resolveMethod();
//            }
//
//            System.err.println("Crud!");
//            System.exit(42);
        }
    }

    private MethodScope resolveMethod() {
        final String methodName = nameLexeme;
        final MethodInvocationExpression invocation = getMethodInvocation();

        // First, determine the expected properties of the method.
        if (qualifier != null) {
            if (qualifier.nameKind == EXPRESSIONNAME) {

            } else if (qualifier.nameKind == TYPENAME) {

            } else {
                System.err.println("Found non expression or type method name qualifier.");
                System.exit(42);
            }
        } else {

        }


        return null;
    }

    private void resolveLeftMostAmbiguousName() {
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

    private void disambiguateInternalQualifiedName() {
        if (nameKind != AMBIGUOUSNAME) return;

        final String qualifierName = getQualifierName();

        // Need to take entire name on the left.
        if (qualifier.nameKind == Kind.PACKAGENAME) {
            final PackageScope packageScope = getParentClass().packageMap.get(qualifierName);

            if (packageScope != null
                    && packageScope.classes.stream().anyMatch(c -> c.name.equals(nameLexeme))) {
                nameKind = TYPENAME;
            } else {
                nameKind = PACKAGENAME;
            }
        } else if (qualifier.nameKind == Kind.TYPENAME) {
            final ClassScope matchingClass = getParentClass()
                    .findClass(qualifier.type.name.getQualifiedName());

            if (matchingClass == null) {
                System.err.println("Could not find typename for ambiguous type name.");
                System.exit(42);
            }

            if (matchingClass.checkIdentifierAgainstFields(nameLexeme)
                    || matchingClass.checkIdentifierAgainstMethods(nameLexeme)) {
                nameKind = EXPRESSIONNAME;
            } else {
                System.err.println("Could not reclassify ambiguous typename.");
                System.exit(42);
            }
        } else if (qualifier.nameKind == Kind.EXPRESSIONNAME) {
            final ClassScope matchingClass = getParentClass()
                    .findClass(qualifier.type.name.getQualifiedName());

            if (matchingClass == null) {
                System.err.println("Could not find typename for ambiguous expression name.");
                System.exit(42);
            }

            if (matchingClass.checkIdentifierAgainstFields(nameLexeme)
                    || matchingClass.checkIdentifierAgainstMethods(nameLexeme)) {
                nameKind = EXPRESSIONNAME;
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
        return ast.isNameExpr();
    }

    private String getQualifierName() {
        if (qualifier != null) {
            return qualifier.getQualifiedName();
        } else {
            return null;
        }
    }

    private String getQualifiedName() {
        if (qualifier != null) {
            return qualifier.getQualifiedName() + "." + nameLexeme;
        } else {
            return nameLexeme;
        }
    }

    private ClassScope getResolvedType() {
        return getParentClass().getClassFromPackage(
                qualifier.type.name.getPackageName().getQualifiedName(),
                qualifier.type.name.getSimpleName());
    }
}
