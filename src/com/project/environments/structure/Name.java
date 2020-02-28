package com.project.environments.structure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Name {
    private final ArrayList<String> fullyQualifiedName;

    public Name(final ArrayList<String> names) {
        fullyQualifiedName = new ArrayList<>();
        for (int i = 0; i < names.size(); ++i) {
            if (i % 2 == 0) {
                fullyQualifiedName.add(names.get(i));
            } else if (!names.get(i).equals(".")) {
                System.err.println("Alternating through name and didn't find dot; aborting!");
                System.exit(42);
            }
        }
    }

    public Name(final String name) {
        fullyQualifiedName = new ArrayList<>();
        fullyQualifiedName.add(name);
    }

    private Name(){
        fullyQualifiedName = new ArrayList<>();
    }

    public static Name generateLangImportName() {
        final Name name = new Name();
        name.fullyQualifiedName.add("lang");
        name.fullyQualifiedName.add("java");
        return name;
    }

    public static Name generateObjectExtendsName() {
        final Name name = new Name();
        name.fullyQualifiedName.add("Object");
        name.fullyQualifiedName.add("lang");
        name.fullyQualifiedName.add("java");
        return name;
    }

    public static Name generateJavaLangPackageName() {
        final Name name = new Name();
        name.fullyQualifiedName.add("lang");
        name.fullyQualifiedName.add("java");
        return name;
    }

    public Name generateAppendedPackageName(final String className) {
        final Name name = new Name();
        name.fullyQualifiedName.add(className);
        name.fullyQualifiedName.addAll(fullyQualifiedName);
        return name;
    }

    public String getQualifiedName() {
        final StringBuilder name = new StringBuilder();
        ArrayList<String> temp = new ArrayList<>(fullyQualifiedName);
        Collections.reverse(temp);
        for (final String n : temp) {
            name.append(n).append(".");
        }
        return name.substring(0, name.length() - 1);
    }

    public String toString() {
        return getQualifiedName();
    }


    public String getSimpleName() {
        return fullyQualifiedName.get(0);
    }

    //TODO: Just returning null string rn but should this ever need to occur?
    public String getClassName() {
        return fullyQualifiedName.size() > 0 ? fullyQualifiedName.get(0) : "null";
    }

    public boolean checkPackageMatch(final Name other) {
        final List<String> packageName = fullyQualifiedName.subList(1, fullyQualifiedName.size());
        return packageName.containsAll(other.fullyQualifiedName)
                && other.fullyQualifiedName.containsAll(packageName);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Name) {
            final Name other = (Name) obj;

            return fullyQualifiedName.containsAll(other.fullyQualifiedName)
                    && other.fullyQualifiedName.containsAll(fullyQualifiedName);
        } else {
            return false;
        }
    }

    public boolean containsSuffixName(final Name name) {
        if (name.fullyQualifiedName.size() > this.fullyQualifiedName.size()) return false;

        for (int i = 0; i < name.fullyQualifiedName.size(); ++i) {
            if (!name.fullyQualifiedName.get(i).equals(this.fullyQualifiedName.get(i))) return false;
        }

        return true;
    }

    public boolean containsPrefixName(final Name name) {
        if (name.fullyQualifiedName.size() > this.fullyQualifiedName.size()) return false;

        for (int i = name.fullyQualifiedName.size() - 1, j = this.fullyQualifiedName.size() - 1
             ;i >= 0 && j >= 0; --i, --j) {
            if (!name.fullyQualifiedName.get(i).equals(this.fullyQualifiedName.get(j))) return false;
        }

        return true;
    }

    public boolean isJavaLang() {
        return fullyQualifiedName.size() == 2
                && fullyQualifiedName.get(0).equals("lang")
                && fullyQualifiedName.get(1).equals("java");
    }
}
