package com.project.heirarchy_checker;

import com.project.environments.ClassScope;
import com.project.environments.structure.Name;

import java.util.ArrayList;
import java.util.HashMap;

public class HierarchyChecker {

    final ArrayList<ClassScope> classTable;

    public HierarchyChecker(final ArrayList<ClassScope> classTable) {
        this.classTable = classTable;
    }

    public boolean followsClassHierarchyRules() {
        ArrayList<String> interfacesSeen = new ArrayList<>();
        ArrayList<String> classesSeen = new ArrayList<>();
        ArrayList<String> extendedClasses = new ArrayList<>();

        for (ClassScope javaClass : classTable) {
            if (javaClass.type == ClassScope.CLASS_TYPE.INTERFACE) {
                if (javaClass.extendsName != null) {
                    return false;
                }
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
                if (javaClass.extendsName != null) {
                    extendedClasses.add(javaClass.extendsName.getSimpleName());
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

            String extendsName = "";
            if (javaClass.extendsName == null) {
                extendsName = null;
            } else {
                extendsName = javaClass.extendsName.getQualifiedName();
            }
            if (interfacesSeen.contains(extendsName)) {
                System.out.println("Class Extending Interface");
                return false;
            }

            if (javaClass.implementsTable != null) {
                for (Name implementsClass : javaClass.implementsTable) {
                    System.out.println(implementsClass.getQualifiedName());
                    if (classesSeen.contains(implementsClass.getQualifiedName())) {
                        System.out.println("Class Implementing Class");
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
