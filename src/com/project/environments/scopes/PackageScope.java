package com.project.environments.scopes;

import java.util.ArrayList;

public class PackageScope {
    public final ArrayList<ClassScope> classes = new ArrayList<>();

    public ClassScope getClass(final String simpleName) {
        for (final ClassScope classScope : classes) {
            if (classScope.name.equals(simpleName)) return classScope;
        }
        return null;
    }

    public void addClass(final ClassScope classScope) {
        classes.add(classScope);
    }

    public boolean containsClass(final String className) {
        return classes.stream().anyMatch(c -> c.name.equals(className));
    }
}
