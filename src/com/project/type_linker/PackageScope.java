package com.project.type_linker;

import com.project.environments.ClassScope;

import java.util.ArrayList;

public class PackageScope {
    final ArrayList<ClassScope> classes = new ArrayList<>();

    public void addClass(final ClassScope classScope) {
        classes.add(classScope);
    }

    public boolean containsClass(final String className) {
        return classes.stream().anyMatch(c -> c.name.equals(className));
    }
}
