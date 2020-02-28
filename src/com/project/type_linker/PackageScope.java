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
}
