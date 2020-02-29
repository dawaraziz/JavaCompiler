package com.project.type_linker;

import com.project.environments.ClassScope;

import java.util.ArrayList;

public class PackageScope {
    String qualifiedName;
    ArrayList<ClassScope> classes = new ArrayList<>();

    public PackageScope(String qualifiedName){
        this.qualifiedName = qualifiedName;
    }

    public void addClassToScope(ClassScope javaClass){
        classes.add(javaClass);
    }

    public boolean containsClass(String className) {
        for (ClassScope includedClass : classes) {
            // Equals simple type or the fully qualified name
            System.out.println(includedClass.name +" BANG " + className);
            if (includedClass.name.equals(className)) {
                return true;
            }
        }
        return false;
    }
}
