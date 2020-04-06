package com.project.hierarchy;

import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.ConstructorScope;
import com.project.environments.scopes.MethodScope;
import com.project.environments.structure.Name;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import static com.project.Main.classTable;
import static com.project.environments.scopes.ClassScope.CLASS_TYPE.CLASS;
import static com.project.environments.scopes.ClassScope.CLASS_TYPE.INTERFACE;

public class HierarchyChecker {

    private final HashMap<String, ClassScope> classMap;

    public HierarchyChecker(final ArrayList<ClassScope> classTable,
                            final HashMap<String, ClassScope> classMap) {
        this.classMap = classMap;
    }

    public void checkSuperCycles() {
        for (final ClassScope classScope : classTable) {

            final String className = classScope.packageName
                    .generateAppendedPackageName(classScope.name)
                    .getDefaultlessQualifiedName();

            if (classScope.extendsTable != null &&
                    classScope.extendsTable.stream()
                            .map(Name::getQualifiedName)
                            .anyMatch(c -> c.equals(className))) {
                System.err.println("Detected a cycle.");
                System.exit(42);
            }

            final ArrayList<String> namesSeen = new ArrayList<>();
            namesSeen.add(className);

            cycleCheck(classScope, className, namesSeen);
        }
    }

    private void cycleCheck(final ClassScope classScope, final String startName,
                            final ArrayList<String> namesSeen) {
        if (classScope == null || classScope.extendsTable == null) return;

        for (final Name superName : classScope.extendsTable) {
            final ClassScope superClass = classMap.get(superName.getDefaultlessQualifiedName());
            final String superQualifiedName = superClass.packageName
                    .generateAppendedPackageName(superClass.name)
                    .getDefaultlessQualifiedName();

            if (namesSeen.size() > 1) {
                if (superQualifiedName.equals(startName)) {
                    System.err.println("Detected a cycle.");
                    System.exit(42);
                }

                if (namesSeen.contains(superQualifiedName)) return;
            }

            namesSeen.add(superQualifiedName);

            cycleCheck(classMap.get(superQualifiedName), startName, namesSeen);
        }
    }


    public void followsClassHierarchyRules() {
        final ArrayList<String> interfacesSeen = new ArrayList<>();
        final ArrayList<String> classesSeen = new ArrayList<>();
        final ArrayList<String> extendedClasses = new ArrayList<>();

        for (final ClassScope javaClass : classTable) {

            if (javaClass.classType == CLASS && javaClass.extendsTable != null) {
                for (final Name name : javaClass.extendsTable) {
                    extendedClasses.add(name.getQualifiedName());
                }
            }

            (javaClass.classType == INTERFACE ? interfacesSeen : classesSeen)
                    .add(javaClass.packageName
                            .generateAppendedPackageName(javaClass.name)
                            .getDefaultlessQualifiedName());
        }

        for (final ClassScope javaClass : classTable) {
            final String name = javaClass.packageName
                    .generateAppendedPackageName(javaClass.name)
                    .getDefaultlessQualifiedName();

            if (extendedClasses.contains(name) && javaClass.modifiers.contains("final")) {
                System.err.println("Class extending a final class");
                System.exit(42);
            }

            if (javaClass.extendsTable != null) {
                for (final Name superClass : javaClass.extendsTable) {
                    if (interfacesSeen.contains(superClass.getQualifiedName())
                            && javaClass.classType == ClassScope.CLASS_TYPE.CLASS) {
                        System.err.println("Class Extending Interface");
                        System.exit(42);
                    }

                    if (classesSeen.contains(superClass.getQualifiedName())
                            && javaClass.classType == INTERFACE) {
                        System.err.println("Interface Extending Class");
                        System.exit(42);
                    }
                }
            }

            if (javaClass.implementsTable != null) {
                for (final Name implementsClass : javaClass.implementsTable) {
                    if (classesSeen.contains(implementsClass.getQualifiedName())
                            && javaClass.classType == ClassScope.CLASS_TYPE.CLASS) {
                        System.err.println("Class Implementing Class");
                        System.exit(42);
                    }
                }
            }
        }
    }


    public void followsMethodHierarchyRules() {
        checkAbstractMethodsInNonAbstractClass();
        checkDuplicateConstructors();
        checkDuplicateMethods();
        checkInvalidOverride();
        checkUnimplementedAbstractMethods();
    }

    private void checkInvalidOverride() {
        for (final ClassScope javaClass : classTable) {
            if (javaClass.methodTable == null) continue;

            final ArrayList<MethodScope> interfaceMethods = getInterfaceMethods(javaClass);
            final ArrayList<MethodScope> superMethods = getSuperMethods(javaClass);

            final ArrayList<MethodScope> inheritedMethods = new ArrayList<>();
            inheritedMethods.addAll(interfaceMethods);
            inheritedMethods.addAll(superMethods);

            for (final MethodScope override : javaClass.methodTable) {
                for (final MethodScope parent : inheritedMethods) {
                    if (!override.equals(parent)) continue;

                    final ArrayList<String> parentMods = parent.modifiers;
                    final ArrayList<String> overrideMods = override.modifiers;

                    if (parentMods.contains("static") ^ overrideMods.contains("static")) {
                        System.err.println("Non static method replacing static.");
                        System.exit(42);
                    } else if (!parent.type.equals(override.type)) {
                        System.err.println("Same signature with different return types.");
                        System.exit(42);
                    } else if (overrideMods.contains("protected") && parentMods.contains("public")) {
                        System.err.println("Protected method replacing public.");
                        System.exit(42);
                    } else if (parent.modifiers.contains("final")) {
                        System.err.println("Method replacing final method.");
                        System.exit(42);
                    }
                }
            }

            for (final MethodScope interfaceMethod : interfaceMethods) {
                for (final MethodScope superMethod : superMethods) {
                    if (!interfaceMethod.equals(superMethod)) continue;

                    if (interfaceMethod.modifiers.contains("public")
                            && superMethod.modifiers.contains("protected")
                            && !javaClass.modifiers.contains("abstract")) {
                        System.err.println("Inherited protected method from super and public method from interface.");
                        System.exit(42);
                    }
                }
            }

            for (int i = 0; i < inheritedMethods.size(); ++i) {
                final MethodScope method_1 = inheritedMethods.get(i);
                for (int j = 0; j < inheritedMethods.size(); ++j) {
                    if (i == j) continue;

                    final MethodScope method_2 = inheritedMethods.get(j);

                    if (method_2.name.equals("getClass") && method_1.name.equals("getClass")) continue;
                    if (method_2.parentScope == null && method_1.parentScope == null) continue;

                    if (!method_2.equals(method_1)) continue;

                    final ArrayList<String> method_1_Mods = method_1.modifiers;
                    final ArrayList<String> method_2_Mods = method_2.modifiers;

                    if (method_1_Mods.contains("static") ^ method_2_Mods.contains("static")) {
                        System.err.println("Non static method replacing static.");
                        System.exit(42);
                    } else if (!method_1.type.equals(method_2.type)) {
                        System.err.println("Same signature with different return types.");
                        System.exit(42);
                    } else if (method_1.modifiers.contains("final")) {
                        System.err.println("Method replacing final method.");
                        System.exit(42);
                    }
                }
            }
        }
    }

    private ArrayList<MethodScope> getInterfaceMethods(final ClassScope javaClass) {
        final ArrayList<MethodScope> inheritedMethods = new ArrayList<>();

        final Stack<ClassScope> classes = new Stack<>();
        classes.push(javaClass);

        while (!classes.isEmpty()) {
            final ClassScope curClass = classes.pop();
            inheritedMethods.addAll(getMethods(curClass.implementsTable, classes));
        }

        return inheritedMethods;
    }

    private ArrayList<MethodScope> getSuperMethods(final ClassScope javaClass) {
        final ArrayList<MethodScope> inheritedMethods = new ArrayList<>();

        final Stack<ClassScope> classes = new Stack<>();
        classes.push(javaClass);

        while (!classes.isEmpty()) {
            final ClassScope curClass = classes.pop();
            inheritedMethods.addAll(getMethods(curClass.extendsTable, classes));
        }

        return inheritedMethods;
    }

    private ArrayList<MethodScope> getMethods(final ArrayList<Name> superList,
                                              final Stack<ClassScope> classes) {
        final ArrayList<MethodScope> methods = new ArrayList<>();

        if (superList == null) return methods;

        for (final Name superName : superList) {
            final ClassScope superClass = classMap.get(superName.getDefaultlessQualifiedName());

            if (superClass == null) continue;

            classes.push(superClass);

            if (superClass.methodTable != null) {
                methods.addAll(superClass.methodTable);
            }
        }

        return methods;

    }

    private void checkUnimplementedAbstractMethods() {
        for (final ClassScope classScope : classTable) {
            checkSuperAbstractMethods(classScope,
                    new ArrayList<>(),
                    !classScope.modifiers.contains("abstract")
                            && classScope.classType != INTERFACE);

        }
    }

    private void checkSuperAbstractMethods(final ClassScope classScope,
                                           final ArrayList<MethodScope> seenMethods,
                                           final boolean hasRealChild) {
        if (classScope.methodTable == null) return;

        seenMethods.addAll(classScope.methodTable);

        if (classScope.extendsTable != null) {
            if (classScope.classType == ClassScope.CLASS_TYPE.INTERFACE) {
                checkInterfaceExtendsAbstractMethods(classScope, seenMethods, hasRealChild);
            } else {
                checkClassExtendsAbstractMethods(classScope, seenMethods, hasRealChild);
            }
        }

        if (classScope.implementsTable != null) {
            checkInterfaceAbstractMethods(classScope, seenMethods, hasRealChild);
        }
    }

    private void checkInterfaceExtendsAbstractMethods(final ClassScope classScope,
                                                      final ArrayList<MethodScope> seenMethods,
                                                      final boolean hasRealChild) {
        for (final Name superName : classScope.extendsTable) {
            final ClassScope superClass = classMap.get(superName.getDefaultlessQualifiedName());
            final boolean isClassAbstract = classScope.modifiers.contains("abstract");

            if (superClass == null) continue;

            if (superClass.methodTable != null) {
                for (final MethodScope methodScope : superClass.methodTable) {
                    if (methodScope.parentScope != null) continue;

                    final boolean foundSameSig = seenMethods.stream()
                            .anyMatch(methodScope::equals);

                    if (!foundSameSig && hasRealChild) {
                        System.err.println("Found interface with unimplemented method.");
                        System.exit(42);
                    }

                }
            }

            checkSuperAbstractMethods(superClass,
                    seenMethods,
                    !isClassAbstract || hasRealChild);
        }
    }

    private void checkClassExtendsAbstractMethods(final ClassScope classScope,
                                                  final ArrayList<MethodScope> seenMethods,
                                                  final boolean hasRealChild) {
        for (final Name superName : classScope.extendsTable) {
            final ClassScope superClass = classMap.get(superName.getDefaultlessQualifiedName());
            final boolean isClassAbstract = classScope.modifiers.contains("abstract");

            if (superClass == null) continue;

            for (final MethodScope methodScope : superClass.methodTable) {
                if (!methodScope.modifiers.contains("abstract")
                        || methodScope.parentScope == null) continue;

                final boolean foundSameSig = seenMethods.stream()
                        .filter(methodScope::equals)
                        .anyMatch(c -> isClassAbstract || c.body != null);

                if (!foundSameSig && !isClassAbstract) {
                    System.err.println("Found class with unimplemented abstract method.");
                    System.exit(42);
                }
            }

            checkSuperAbstractMethods(superClass,
                    seenMethods,
                    !isClassAbstract || hasRealChild);
        }
    }

    private void checkInterfaceAbstractMethods(final ClassScope classScope,
                                               final ArrayList<MethodScope> seenMethods,
                                               final boolean hasRealChild) {
        for (final Name superName : classScope.implementsTable) {
            final ClassScope superClass = classMap.get(superName.getDefaultlessQualifiedName());

            if (superClass == null) continue;

            if (superClass.methodTable != null) {
                for (final MethodScope methodScope : superClass.methodTable) {
                    if (seenMethods.stream().anyMatch(methodScope::equals)) continue;

                    if (!classScope.modifiers.contains("abstract") || hasRealChild) {
                        System.err.println("Found interface with unimplemented method.");
                        System.exit(42);
                    }
                }
            }

            checkSuperAbstractMethods(superClass,
                    seenMethods,
                    !classScope.modifiers.contains("abstract") || hasRealChild);
        }
    }

    private void checkAbstractMethodsInNonAbstractClass() {
        for (final ClassScope javaClass : classTable) {
            if (javaClass.methodTable == null
                    || javaClass.classType == INTERFACE
                    || javaClass.modifiers.contains("abstract")) continue;

            for (final MethodScope method : javaClass.methodTable) {
                if (method.modifiers.contains("abstract")) {
                    System.err.println("Found non-abstract class with abstract method.");
                    System.exit(42);
                }
            }
        }
    }

    private void checkDuplicateMethods() {
        for (final ClassScope classScope : classTable) {
            final ArrayList<MethodScope> methods = classScope.methodTable;

            if (methods == null) continue;

            // Brute force through the methods to find duplicates.
            if (hasOrderedDuplicates(methods)) {
                System.err.println("Found duplicate methods.");
                System.exit(42);
            }
        }
    }

    private void checkDuplicateConstructors() {
        for (final ClassScope classScope : classTable) {
            final ArrayList<ConstructorScope> constructors = classScope.constructorTable;

            if (constructors == null) {
                System.err.println("Found class with no constructor; aborting!");
                System.exit(42);
            }

            // Brute force through the constructors to find duplicates.
            if (hasOrderedDuplicates(constructors)) {
                System.err.println("Found duplicate constructors.");
                System.exit(42);
            }
        }
    }

    private <T> boolean hasOrderedDuplicates(final ArrayList<T> list) {
        for (int i = 0; i < list.size(); ++i) {
            for (int j = i + 1; j < list.size(); ++j) {
                if (list.get(i).equals(list.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }
}
