package com.project.heirarchy_checker;

import com.project.environments.ClassScope;
import com.project.environments.ConstructorScope;
import com.project.environments.MethodScope;
import com.project.environments.structure.Name;
import com.project.environments.structure.Parameter;

import java.util.ArrayList;
import java.util.HashMap;

public class HierarchyChecker {

    final ArrayList<ClassScope> classTable;
    final HashMap<String, ClassScope> classMap;

    public HierarchyChecker(final ArrayList<ClassScope> classTable, final HashMap<String, ClassScope> classMap) {
        this.classTable = classTable;
        this.classMap = classMap;
    }

    public boolean cycleDetected() {

        for (ClassScope javaClass : classTable) {
            ArrayList<String> namesSeen = new ArrayList<>();
            String name = "";
            if (javaClass.packageName == null) name = javaClass.name;
            else name = javaClass.packageName.getQualifiedName() + "." + javaClass.name;
            namesSeen.add(name);
            if (cycleCheck(javaClass, namesSeen)) {
                System.out.println("Detected a cycle");
                return true;
            }
        }

        return false;
    }

    private boolean cycleCheck(ClassScope javaClass, ArrayList<String> namesSeen) {
        if (javaClass.extendsTable != null) {
            for (Name extendsName : javaClass.extendsTable) {
                if (namesSeen.contains(extendsName.getQualifiedName())) return true;
                else namesSeen.add(extendsName.getQualifiedName());
                ClassScope classScope = classMap.get(extendsName.getQualifiedName());
                if (classScope == null) {
                    int a = 1;
                }
                if (classScope == null) return false;
                if (cycleCheck(classScope, namesSeen)) return true;
            }
        }

        return false;
    }


    public boolean followsClassHierarchyRules() {
        ArrayList<String> interfacesSeen = new ArrayList<>();
        ArrayList<String> classesSeen = new ArrayList<>();
        ArrayList<String> extendedClasses = new ArrayList<>();

        for (ClassScope javaClass : classTable) {
            if (javaClass.type == ClassScope.CLASS_TYPE.INTERFACE) {
                String name = "";
                if (javaClass.packageName == null) {
                    name = javaClass.name;
                }
                else {
                    name = javaClass.packageName + "." + javaClass.name;
                }
                interfacesSeen.add((name));
            }

            else {
                if (javaClass.extendsTable != null) {
                    for (Name name : javaClass.extendsTable) {
                        extendedClasses.add(name.getQualifiedName());
                    }
                }

                String name = "";
                if (javaClass.packageName == null) {
                    name = javaClass.name;
                }
                else {
                    name = javaClass.packageName + "." + javaClass.name;
                }
                classesSeen.add((name));
            }
        }

        for (ClassScope javaClass: classTable) {

            if (extendedClasses.contains(javaClass.name) && javaClass.modifiers.contains("final")) {
                System.out.println("Class extending a final class");
                return false;
            }


            if (javaClass.extendsTable != null) {
                for (Name extendsClass: javaClass.extendsTable) {
                    if (interfacesSeen.contains(extendsClass.getQualifiedName()) && javaClass.type == ClassScope.CLASS_TYPE.CLASS) {
                        System.out.println("Class Extending Interface");
                        return false;
                    }

                    if (classesSeen.contains(extendsClass.getQualifiedName()) && javaClass.type == ClassScope.CLASS_TYPE.INTERFACE) {
                        System.out.println("Interface Extending Class");
                        return false;
                    }
                }
            }

            if (javaClass.implementsTable != null) {
                for (Name implementsClass : javaClass.implementsTable) {
                    if (classesSeen.contains(implementsClass.getQualifiedName()) && javaClass.type == ClassScope.CLASS_TYPE.CLASS) {
                        System.out.println("Class Implementing Class");
                        return false;
                    }
                }
            }
        }

        return true;
    }


    public boolean followsMethodHierarchyRules() {

        if (!noDuplicateConstructors()) return false;

        if (abstractMethodCheck()) return false;

        if (checkForDuplicateMethods()) return false;

        return true;
    }

    private boolean checkForDuplicateMethods() {
        for (ClassScope javaClass: classTable) {
            if (javaClass.methodTable != null) {
                HashMap<String, ArrayList<Parameter>> methodSignatures = new HashMap();
                for (MethodScope method: javaClass.methodTable) {
                    if (methodSignatures.containsKey(method.name)) {
                        ArrayList<Parameter> params = methodSignatures.get(method.name);
                        if (params == null && method.parameters == null) {
                            System.out.println("Duplicate method signatures");
                            return true;
                        }
                        else if (params != null && method.parameters != null) {
                            if (params.size() == method.parameters.size() && params.containsAll(method.parameters) && method.parameters.containsAll(params)) {
                                System.out.println("Duplicate method signatures");
                                return true;
                            }
                        }
                    }
                    else {
                        methodSignatures.put(method.name, method.parameters);
                    }
                }
            }
        }

        return false;
    }

    private boolean abstractMethodCheck() {
        for (ClassScope javaClass: classTable) {
            if (javaClass.methodTable != null) {
                for (MethodScope method : javaClass.methodTable) {
                    if (method.modifiers.contains("abstract") && !javaClass.modifiers.contains("abstract")) {
                        System.out.println("Non abstract class with abstract method");
                        return true;
                    }
                }
            }
            /* Gotta figure out how to check for abstract methods that are inherited and implemented*/
//            if (foundAbstractMethod(javaClass) && !javaClass.modifiers.contains("abstract")) {
//                System.out.println("Non abstract class with abstract method");
//                return true;
//            }
        }
        return false;
    }

    private boolean foundAbstractMethod (ClassScope javaClass) {

        if (javaClass.extendsTable != null) {
            for (Name extendClassName : javaClass.extendsTable) {
                String name = extendClassName.getQualifiedName();
                ClassScope extendClass = classMap.get(name);

                if (extendClass != null) {
                    if (extendClass.methodTable != null) {
                        for (MethodScope method : extendClass.methodTable) {
                            if (method.modifiers.contains("abstract")) {
                                return true;
                            }
                        }
                    }
                    if (foundAbstractMethod(extendClass)) return true;
                }
            }
        }

        return false;
    }

    private boolean noDuplicateConstructors() {
        for (ClassScope javaClass: classTable) {
            ArrayList<ArrayList<Parameter>> paramsList = new ArrayList<>();
            if (javaClass.constructorTable.size() > 1) {
                for (ConstructorScope constructor : javaClass.constructorTable) {
                    paramsList.add(constructor.parameters);
                }
                int i = 0;
                while (i < paramsList.size()) {
                    int j = 0;
                    while (j < paramsList.size()) {
                        if (i != j) {
                            if (paramsList.get(i) == null && paramsList.get(j) == null) {
                                System.out.println("Constructors with same parameter types");
                                return false;
                            }

                            else if (paramsList.get(i) != null && paramsList.get(j) != null) {
                                if (paramsList.get(i).size() == paramsList.get(j).size()) {
                                    if (paramsList.get(i).containsAll(paramsList.get(j)) && paramsList.get(j).containsAll(paramsList.get(i))) {
                                        System.out.println("Constructors with same parameter types");
                                        return false;
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
        return true;
    }
}
