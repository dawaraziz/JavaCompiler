package com.project.heirarchy_checker;

import com.project.environments.ClassScope;
import com.project.environments.structure.Name;

import java.util.ArrayList;
import java.util.HashMap;

public class HeirarchyChecker {

    final ArrayList<ClassScope> classTable;

    public HeirarchyChecker(final ArrayList<ClassScope> classTable) {
        this.classTable = classTable;
    }

    public boolean classExtendsInterface() {
        ArrayList<String> interfacesSeen = new ArrayList<>();

        for (ClassScope javaClass : classTable) {
            if (javaClass.type == ClassScope.CLASS_TYPE.INTERFACE) {
                interfacesSeen.add((javaClass.name));
            }
        }

        for (ClassScope javaClass: classTable) {
            if (interfacesSeen.contains(javaClass.extendsName)) {
                return true;
            }
        }

        return false;
    }
}