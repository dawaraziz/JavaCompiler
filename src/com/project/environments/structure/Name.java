package com.project.environments.structure;

import java.util.ArrayList;
import java.util.Arrays;
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

    private Name() {
        fullyQualifiedName = new ArrayList<>();
    }

    public boolean isNotSimpleName() {
        final boolean notSimple = this.getQualifiedName().contains(".");
        System.out.println("Is there a dot? guaranteed " + notSimple + " in " + this.getQualifiedName());
        return notSimple;
    }

    public static Name generateFullyQualifiedName(final String simpleName, final Name packageName) {
        final Name name = new Name();
        name.fullyQualifiedName.add(simpleName);
        name.fullyQualifiedName.addAll(packageName.fullyQualifiedName);
        return name;
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
        final ArrayList<String> temp = new ArrayList<>(fullyQualifiedName);
        Collections.reverse(temp);
        for (final String n : temp) {
            name.append(n).append(".");
        }
        return name.substring(0, name.length() - 1);
    }

    public String getDefaultlessQualifiedName() {
        final String qualifiedName = getQualifiedName();
        return qualifiedName.contains("default#") ? qualifiedName.substring(9) : qualifiedName;
    }

    public String toString() {
        return getQualifiedName();
    }


    public String getPackageString() {
        //this excludes the class unlike qualified name
        final String qualifiedName = getQualifiedName();
        final StringBuilder name = new StringBuilder();
        final ArrayList<String> fullyQualifiedName = new ArrayList<>(Arrays.asList(qualifiedName.split("\\.")));
        for (int i = 0; i < fullyQualifiedName.size() - 1; i++) {
            final String n = fullyQualifiedName.get(i);
            name.append(n).append(".");
        }
        return name.substring(0, name.length() - 1);
    }

    public String getActualSimpleName() {
        final String qualifiedName = getQualifiedName();
        final ArrayList<String> fullQualifiedName = new ArrayList<>(Arrays.asList(qualifiedName.split("\\.")));
        return fullQualifiedName.get(fullQualifiedName.size() - 1);
    }

    public String getSimpleName() {
        return fullyQualifiedName.get(0);
    }

    public Name getPackageName() {
        if (fullyQualifiedName.size() == 1) return null;

        final Name name = new Name();
        name.fullyQualifiedName.addAll(fullyQualifiedName.subList(1, fullyQualifiedName.size()));
        return name;
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

    public static boolean containsPrefixName(final Name check, final Name prefix) {
        final ArrayList<String> checkList = check.fullyQualifiedName;
        final ArrayList<String> prefixList = prefix.fullyQualifiedName;

        if (checkList.size() < prefixList.size()) return false;

        for (int i = checkList.size() - 1, j = prefixList.size() - 1; i >= 0 && j >= 0; --i, --j) {
            if (!checkList.get(i).equals(prefixList.get(j))) return false;
        }

        return true;
    }

    public boolean isJavaLang() {
        return fullyQualifiedName.size() == 2
                && fullyQualifiedName.get(0).equals("lang")
                && fullyQualifiedName.get(1).equals("java");
    }

    public boolean isDefault() {
        return fullyQualifiedName.size() == 1 && fullyQualifiedName.get(0).equals("default#");
    }
}
