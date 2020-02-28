package com.project.environments.structure;

import java.util.ArrayList;
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

    public String getQualifiedName() {
        final StringBuilder name = new StringBuilder();
        for (final String n : fullyQualifiedName) {
            name.append(n).append(".");
        }
        return name.substring(0, name.length() - 1);
    }


    public String getSimpleName() {
        return fullyQualifiedName.get(fullyQualifiedName.size() - 1);
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
    public boolean equals(Object obj) {
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

    public boolean checkClassName() {
        return false;
    }

    public boolean isJavaLang() {
        return fullyQualifiedName.size() == 2
                && fullyQualifiedName.get(0).equals("lang")
                && fullyQualifiedName.get(1).equals("java");
    }
}
