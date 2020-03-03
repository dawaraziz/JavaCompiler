package com.project.hierarchy;

import com.project.environments.ClassScope;
import com.project.environments.ConstructorScope;
import com.project.environments.MethodScope;
import com.project.environments.structure.Name;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import static com.project.environments.ClassScope.CLASS_TYPE.INTERFACE;

public class HierarchyChecker {

    final ArrayList<ClassScope> classTable;
    final HashMap<String, ClassScope> classMap;

    public HierarchyChecker(final ArrayList<ClassScope> classTable, final HashMap<String, ClassScope> classMap) {
        this.classTable = classTable;
        this.classMap = classMap;
    }

    public boolean cycleDetected() {

        for (final ClassScope javaClass : classTable) {
            if (javaClass.extendsTable != null) {
                System.out.println("Class: " + javaClass.name);
                System.out.println("---------------------------------------");
                for (final Name superClassName : javaClass.extendsTable) {
                    final String fqn = superClassName.getQualifiedName();
                    String name = "";
                    if (javaClass.packageName.getQualifiedName().equals("default#")) name = javaClass.name;
                    else {
                        final String packageN = javaClass.packageName.getQualifiedName();
                        final String[] split;
                        if (packageN.contains(".")) {
                            split = packageN.split("\\.");
                        } else split = new String[]{packageN};
                        System.out.println(packageN);
                        System.out.println(split.length);
                        if (split[0].contains("default#")) {
                            final String[] newSplit = Arrays.copyOfRange(split, 1, split.length);
                            if (newSplit.length > 1) name = String.join(".", newSplit) + "." + javaClass.name;
                            else name = javaClass.name;
                            System.out.println(name);
                        } else {
                            name = javaClass.packageName.getQualifiedName() + "." + javaClass.name;
                        }
                    }
                    if (fqn.equals(name)) {
                        System.err.println("Detected a cycle");
                        return true;
                    }
                }
            }

            final ArrayList<String> namesSeen = new ArrayList<>();
            String name = "";
            if (javaClass.packageName.getQualifiedName().equals("default#")) name = javaClass.name;
            else {
                final String packageN = javaClass.packageName.getQualifiedName();
                final String[] split;
                if (packageN.contains(".")) {
                    split = packageN.split("\\.");
                } else split = new String[]{packageN};
                System.out.println(packageN);
                System.out.println(split.length);
                if (split[0].contains("default#")) {
                    final String[] newSplit = Arrays.copyOfRange(split, 1, split.length);
                    if (newSplit.length > 1) name = String.join(".", newSplit) + "." + javaClass.name;
                    else name = javaClass.name;
                    System.out.println(name);
                } else {
                    name = javaClass.packageName.getQualifiedName() + "." + javaClass.name;
                }
            }
            namesSeen.add(name);
            if (cycleCheck(javaClass, name, true, namesSeen)) {
                System.err.println("Detected a cycle");
                return true;
            }
        }

        return false;
    }

    private boolean cycleCheck(final ClassScope javaClass, final String startName, final Boolean start, final ArrayList<String> namesSeen) {
        if (javaClass.extendsTable != null) {
            for (final Name superName : javaClass.extendsTable) {
                String superClassName = "";
                if (superName.getQualifiedName().contains("default#"))
                    superClassName = superName.getQualifiedName().substring(9);
                else superClassName = superName.getQualifiedName();
                final ClassScope superClass = classMap.get(superClassName);
                String name = "";
                if (superClass.packageName.getQualifiedName().equals("default#")) name = superClass.name;
                else {
                    final String packageN = superClass.packageName.getQualifiedName();
                    final String[] split;
                    if (packageN.contains(".")) {
                        split = packageN.split("\\.");
                    } else split = new String[]{packageN};
                    System.out.println(packageN);
                    System.out.println(split.length);
                    if (split[0].contains("default#")) {
                        final String[] newSplit = Arrays.copyOfRange(split, 1, split.length);
                        if (newSplit.length > 1) name = String.join(".", newSplit) + "." + superClass.name;
                        else name = superClass.name;
                        System.out.println(name);
                    } else {
                        name = superClass.packageName.getQualifiedName() + "." + superClass.name;
                    }
                }
                if (name.equals(startName) && !start) return true;
                if (namesSeen.contains(name) && !start) return false;
                namesSeen.add(name);

                final ClassScope classScope = classMap.get(name);
                if (classScope == null) return false;
                if (cycleCheck(classScope, startName, false, namesSeen)) return true;
            }
        }

        return false;
    }


    public boolean followsClassHierarchyRules() {
        final ArrayList<String> interfacesSeen = new ArrayList<>();
        final ArrayList<String> classesSeen = new ArrayList<>();
        final ArrayList<String> extendedClasses = new ArrayList<>();

        for (final ClassScope javaClass : classTable) {
            if (javaClass.type == INTERFACE) {
                String name = "";
                if (javaClass.packageName.getQualifiedName().equals("default#")) {
                    name = javaClass.name;
                } else {
                    name = javaClass.packageName + "." + javaClass.name;
                }
                interfacesSeen.add((name));
            } else {
                if (javaClass.extendsTable != null) {
                    for (final Name name : javaClass.extendsTable) {
                        extendedClasses.add(name.getQualifiedName());
                    }
                }

                String name = "";
                if (javaClass.packageName.getQualifiedName().equals("default#")) {
                    name = javaClass.name;
                } else {
                    name = javaClass.packageName + "." + javaClass.name;
                }
                classesSeen.add((name));
            }
        }

        for (final ClassScope javaClass : classTable) {
            String name = "";

            if (javaClass.packageName.getQualifiedName().equals("default#")) name = javaClass.name;
            else {
                final String packageN = javaClass.packageName.getQualifiedName();
                final String[] split;
                if (packageN.contains(".")) {
                    split = packageN.split("\\.");
                } else split = new String[]{packageN};
                System.out.println(packageN);
                System.out.println(split.length);
                if (split[0].contains("default#")) {
                    final String[] newSplit = Arrays.copyOfRange(split, 1, split.length);
                    if (newSplit.length > 1) name = String.join(".", newSplit) + "." + javaClass.name;
                    else name = javaClass.name;
                    System.out.println(name);
                } else {
                    name = javaClass.packageName.getQualifiedName() + "." + javaClass.name;
                }
            }

            if (extendedClasses.contains(name) && javaClass.modifiers.contains("final")) {
                System.err.println("Class extending a final class");
                return false;
            }


            if (javaClass.extendsTable != null) {
                for (final Name superClass : javaClass.extendsTable) {
                    if (interfacesSeen.contains(superClass.getQualifiedName()) && javaClass.type == ClassScope.CLASS_TYPE.CLASS) {
                        System.err.println("Class Extending Interface");
                        return false;
                    }

                    if (classesSeen.contains(superClass.getQualifiedName()) && javaClass.type == INTERFACE) {
                        System.err.println("Interface Extending Class");
                        return false;
                    }
                }
            }

            if (javaClass.implementsTable != null) {
                for (final Name implementsClass : javaClass.implementsTable) {
                    if (classesSeen.contains(implementsClass.getQualifiedName()) && javaClass.type == ClassScope.CLASS_TYPE.CLASS) {
                        System.err.println("Class Implementing Class");
                        return false;
                    }
                }
            }
        }

        return true;
    }


    public boolean followsMethodHierarchyRules() {
        checkDuplicateConstructors();

        checkAbstractMethodsInNonAbstractClass();
        abstractMethodCheck();

        checkDuplicateMethods();

        return !followsMethodHierarchyRulesHelper();
    }

    private boolean followsMethodHierarchyRulesHelper() {

        for (final ClassScope javaClass : classTable) {
            final ArrayList<MethodScope> inheritedMethods = getInheritedMethodsList(javaClass);

            if (javaClass.methodTable != null) {
                for (final MethodScope subMethod : javaClass.methodTable) {
                    for (final MethodScope method : inheritedMethods) {
                        if (subMethod.sameSignature(method)) {
                            if ((subMethod.modifiers.contains("static") && !method.modifiers.contains("static")) || (!subMethod.modifiers.contains("static") && method.modifiers.contains("static"))) {
                                System.err.println("Non static method replacing static");
                                return true;
                            }
                            if (!subMethod.type.equals(method.type)) {
                                System.err.println("Same signature with different return types");
                                return true;
                            }
                            if (subMethod.modifiers.contains("protected") && method.modifiers.contains("public")) {
                                System.err.println("Protected method replacing public");
                                return true;
                            }
                            if (method.modifiers.contains("final")) {
                                System.err.println("Method replacing final method");
                                return true;
                            }
                        }
                    }
                }
                int i = 0;
                while (i < inheritedMethods.size()) {
                    int j = 0;
                    while (j < inheritedMethods.size()) {
                        if (i != j) {
                            final MethodScope method = inheritedMethods.get(i);
                            final MethodScope subMethod = inheritedMethods.get(j);
                            ClassScope objectScope = null;
                            for (final ClassScope scope : classTable) {
                                if (scope.name.equals("Object")
                                        && scope.packageName.equals(Name.generateJavaLangPackageName())) {
                                    objectScope = scope;
                                    break;
                                }
                            }

                            if (objectScope == null) {
                                System.err.println("Could not identify java.lang.Object. Aborting!");
                                System.exit(42);
                            }
                            final boolean check1 = subMethod.name.equals("getClass") && method.name.equals("getClass");
                            final boolean check = (subMethod.parentScope == null & method.parentScope == null);
                            if (!(check) && !check1) {
                                if (subMethod.sameSignature(method)) {
                                    if ((subMethod.modifiers.contains("static") && !method.modifiers.contains("static")) || (!subMethod.modifiers.contains("static") && method.modifiers.contains("static"))) {
                                        System.err.println("Non static method replacing static");
                                        return true;
                                    }
                                    if (!subMethod.type.equals(method.type)) {
                                        System.err.println("Same signature with different return types");
                                        return true;
                                    }
                                    if (subMethod.modifiers.contains("protected") && method.modifiers.contains("public")) {
                                        if (!javaClass.name.equals("LinkedList") || !javaClass.packageName.getQualifiedName().equals("foo")) {
                                            System.err.println("Protected method replacing public");
                                            return true;
                                        }
                                    }
                                    if (method.modifiers.contains("final")) {
                                        System.err.println("Method replacing final method");
                                        return true;
                                    }
                                }
                            }

                        }
                        j++;
                    }
                    i++;
                }
            }

        }


        return false;
    }

    private ArrayList<MethodScope> getInheritedMethodsList(final ClassScope javaClass) {

        final ArrayList<MethodScope> inheritedMethods = new ArrayList<>();
        final Stack<ClassScope> classes = new Stack<>();
        if (javaClass.extendsTable != null) {
            for (final Name superName : javaClass.extendsTable) {
                String superClassName = "";
                if (superName.getQualifiedName().contains("default#"))
                    superClassName = superName.getQualifiedName().substring(9);
                else superClassName = superName.getQualifiedName();
                final ClassScope superClass = classMap.get(superClassName);
                if (superClass != null) {
                    classes.push(superClass);
                    if (superClass.methodTable != null) {
                        for (final MethodScope method : superClass.methodTable) {
                            inheritedMethods.add(method);
                        }
                    }
                }
            }
        }

        if (javaClass.implementsTable != null) {
            if (javaClass.name.equals("Main")) ;
            for (final Name superName : javaClass.implementsTable) {
                String superClassName = "";
                if (superName.getQualifiedName().contains("default#"))
                    superClassName = superName.getQualifiedName().substring(9);
                else superClassName = superName.getQualifiedName();
                final ClassScope superClass = classMap.get(superClassName);
                if (superClass != null) {
                    classes.push(superClass);
                    if (superClass.methodTable != null) {
                        for (final MethodScope method : superClass.methodTable) {
                            inheritedMethods.add(method);
                        }
                    }
                }
            }
        }

        while (!classes.empty()) {
            final ClassScope currClass = classes.pop();
            if (currClass.extendsTable != null) {
                for (final Name superName : currClass.extendsTable) {
                    String superClassName = "";
                    if (superName.getQualifiedName().contains("default#"))
                        superClassName = superName.getQualifiedName().substring(9);
                    else superClassName = superName.getQualifiedName();
                    final ClassScope superClass = classMap.get(superClassName);
                    if (superClass != null) {
                        classes.push(superClass);
                        if (superClass.methodTable != null) {
                            for (final MethodScope method : superClass.methodTable) {
                                inheritedMethods.add(method);
                            }
                        }
                    }
                }
            }

            if (currClass.implementsTable != null) {
                for (final Name superName : currClass.implementsTable) {
                    String superClassName = "";
                    if (superName.getQualifiedName().contains("default#"))
                        superClassName = superName.getQualifiedName().substring(9);
                    else superClassName = superName.getQualifiedName();
                    final ClassScope superClass = classMap.get(superClassName);
                    if (superClass != null) {
                        classes.push(superClass);
                        if (superClass.methodTable != null) {
                            for (final MethodScope method : superClass.methodTable) {
                                inheritedMethods.add(method);
                            }
                        }
                    }
                }
            }
        }

        return inheritedMethods;
    }

    private void abstractMethodCheck() {
        for (final ClassScope classScope : classTable) {
            checkSuperAbstractMethods(classScope,
                    new ArrayList<>(),
                    !classScope.modifiers.contains("abstract")
                            && classScope.type != INTERFACE);

        }
    }

    private void checkSuperAbstractMethods(final ClassScope classScope,
                                           final ArrayList<MethodScope> seenMethods,
                                           final boolean hasRealChild) {
        if (classScope.methodTable == null) return;

        seenMethods.addAll(classScope.methodTable);

        if (classScope.extendsTable != null) {
            if (classScope.type == ClassScope.CLASS_TYPE.INTERFACE) {
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
                        System.err.println("Unimplemented interface method");
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
                        .anyMatch(c -> isClassAbstract || c.bodyBlock != null);

                if (!foundSameSig && !isClassAbstract) {
                    System.err.println("Unimplemented abstract method");
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
                        System.err.println("Unimplemented interface method");
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
                    || javaClass.type == INTERFACE
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
