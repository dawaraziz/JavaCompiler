package com.project.heirarchy_checker;

import com.project.environments.ClassScope;
import com.project.environments.ConstructorScope;
import com.project.environments.ImportScope;
import com.project.environments.MethodScope;
import com.project.environments.structure.Name;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class HierarchyChecker {

    final ArrayList<ClassScope> classTable;
    final HashMap<String, ClassScope> classMap;

    public HierarchyChecker(final ArrayList<ClassScope> classTable, final HashMap<String, ClassScope> classMap) {
        this.classTable = classTable;
        this.classMap = classMap;
    }

    public boolean cycleDetected() {

        for (ClassScope javaClass : classTable) {
            if (javaClass.extendsTable != null) {
                System.out.println(javaClass.name);
                System.out.println("---------------------------------------");
                for (Name extendClassName : javaClass.extendsTable) {
                    String fqn = extendClassName.getQualifiedName();
                    String name = "";
                    System.out.println(fqn);
                    if (javaClass.packageName == null) name = javaClass.name;
                    else name = javaClass.packageName.getQualifiedName() + "." + javaClass.name;
                    System.out.println(name);
                    if (fqn.equals(name)) {
                        System.err.println("Detected a cycle");
                        return true;
                    }
                }
            }


            ArrayList<String> namesSeen = new ArrayList<>();
            String name = "";
            if (javaClass.packageName == null) name = javaClass.name;
            else name = javaClass.packageName.getQualifiedName() + "." + javaClass.name;
            namesSeen.add(name);
            if (cycleCheck(javaClass, namesSeen)) {
                System.err.println("Detected a cycle");
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

            if (javaClass.imports != null) {
                if (javaClass.name.equals("List"));
                for (ImportScope importScope: javaClass.imports) {
                    String name = importScope.name.getSimpleName();
                    if (javaClass.name.equals(name)) {
                        String qualifiedName = "";
                        if (javaClass.packageName == null) qualifiedName = javaClass.name;
                        else qualifiedName = javaClass.packageName.getQualifiedName() + "." + javaClass.name;
                        if (!qualifiedName.equals(importScope.name.getQualifiedName())) {
                            System.err.println("Clashing Import and Name");
                            return false;
                        }
                    }
                }
            }

            if (extendedClasses.contains(javaClass.name) && javaClass.modifiers.contains("final")) {
                System.err.println("Class extending a final class");
                return false;
            }


            if (javaClass.extendsTable != null) {
                for (Name extendsClass: javaClass.extendsTable) {
                    if (interfacesSeen.contains(extendsClass.getQualifiedName()) && javaClass.type == ClassScope.CLASS_TYPE.CLASS) {
                        System.err.println("Class Extending Interface");
                        return false;
                    }

                    if (classesSeen.contains(extendsClass.getQualifiedName()) && javaClass.type == ClassScope.CLASS_TYPE.INTERFACE) {
                        System.err.println("Interface Extending Class");
                        return false;
                    }
                }
            }

            if (javaClass.implementsTable != null) {
                for (Name implementsClass : javaClass.implementsTable) {
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

        if (!noDuplicateConstructors()) return false;

        if (abstractMethodCheck()) return false;

        if (checkForDuplicateMethods()) return false;

        if (followsMethodHierarchyRulesHelper()) return false;


        return true;
    }

    private boolean followsMethodHierarchyRulesHelper() {

        for (ClassScope javaClass: classTable) {
            ArrayList<MethodScope> inheritedMethods = getInheritedMethodsList(javaClass);


            if (javaClass.methodTable != null) {
                for (MethodScope subMethod : javaClass.methodTable) {
                    for (MethodScope method : inheritedMethods) {
                        System.out.println(subMethod);
                        System.out.println(method);
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
            }

        }


        return false;
    }

    private ArrayList<MethodScope> getInheritedMethodsList(ClassScope javaClass) {
        ArrayList<MethodScope> inheritedMethods = new ArrayList<>();
        Stack<ClassScope> classes = new Stack<>();
        if (javaClass.extendsTable != null) {
            for (Name extendName : javaClass.extendsTable) {
                ClassScope extendClass = classMap.get(extendName.getQualifiedName());
                if (extendClass != null) {
                    classes.push(extendClass);
                    if (extendClass.methodTable != null) {
                        for (MethodScope method: extendClass.methodTable) {
                            inheritedMethods.add(method);
                        }
                    }
                }
            }
        }

        if (javaClass.implementsTable != null) {
            for (Name extendName : javaClass.implementsTable) {
                ClassScope extendClass = classMap.get(extendName.getQualifiedName());
                if (extendClass != null) {
                    classes.push(extendClass);
                    if (extendClass.methodTable != null) {
                        for (MethodScope method: extendClass.methodTable) {
                            inheritedMethods.add(method);
                        }
                    }
                }
            }
        }

        while (!classes.empty()) {
            System.out.println("Hello 55");
            ClassScope currClass = classes.pop();
            if (currClass.extendsTable != null) {
                for (Name extendName : currClass.extendsTable) {
                    ClassScope extendClass = classMap.get(extendName.getQualifiedName());
                    if (extendClass != null) {
                        classes.push(extendClass);
                        if (extendClass.methodTable != null) {
                            for (MethodScope method: extendClass.methodTable) {
                                inheritedMethods.add(method);
                            }
                        }
                    }
                }
            }

            if (currClass.implementsTable != null) {
                for (Name extendName : currClass.implementsTable) {
                    ClassScope extendClass = classMap.get(extendName.getQualifiedName());
                    if (extendClass != null) {
                        classes.push(extendClass);
                        if (extendClass.methodTable != null) {
                            for (MethodScope method: extendClass.methodTable) {
                                inheritedMethods.add(method);
                            }
                        }
                    }
                }
            }
        }

        return inheritedMethods;
    }



    private boolean abstractMethodCheck() {
        for (ClassScope javaClass: classTable) {
            if (javaClass.methodTable != null && javaClass.type != ClassScope.CLASS_TYPE.INTERFACE) {
                for (MethodScope method : javaClass.methodTable) {
                    if (method.modifiers.contains("abstract") && !javaClass.modifiers.contains("abstract")) {
                        System.err.println("Non abstract class with abstract method");
                        return true;
                    }
                }


//                ArrayList<MethodScope> inheritedMethods = getInheritedMethodsList(javaClass);
//                inheritedMethods.addAll(javaClass.methodTable);
//
//                int i = 0;
//                while (i < inheritedMethods.size()) {
//                    int j = 0;
//                    MethodScope method1 = inheritedMethods.get(i);
//
//                    ArrayList<MethodScope> abstractMethods = new ArrayList<>();
//
//                    if (method1.modifiers.contains("abstract")) {
//                        abstractMethods.add(method1);
//                    }
//                    i++;
//                }
//
//                    while (j < inheritedMethods.size()) {
//                        if (i != j) {
//                            MethodScope method2 = inheritedMethods.get(j);
//
//                            if (method1.sameSignature(method2)) {
//                                if (method1.modifiers.contains("abstract")) {
//
//                                }
//                            }
//
//                        }
//                    }
//                }
            }

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


    private boolean checkForDuplicateMethods() {
        for (ClassScope javaClass: classTable) {

            if (javaClass.methodTable != null) {
                HashMap<String, ArrayList<Parameter>> methodSignatures = new HashMap();
                for (MethodScope method: javaClass.methodTable) {
                    if (methodSignatures.containsKey(method.name)) {
                        ArrayList<Parameter> params = methodSignatures.get(method.name);
                        if (params == null && method.parameters == null) {
                            System.err.println("Duplicate method signatures");
                            return true;
                        }
                        else if (params != null && method.parameters != null) {
                            if (params.size() == method.parameters.size() && params.containsAll(method.parameters) && method.parameters.containsAll(params)) {
                                System.err.println("Duplicate method signatures");
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
                                System.err.println("Constructors with same parameter types");
                                return false;
                            }

                            else if (paramsList.get(i) != null && paramsList.get(j) != null) {
                                if (paramsList.get(i).size() == paramsList.get(j).size()) {
                                    if (paramsList.get(i).containsAll(paramsList.get(j)) && paramsList.get(j).containsAll(paramsList.get(i))) {
                                        System.err.println("Constructors with same parameter types");
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
