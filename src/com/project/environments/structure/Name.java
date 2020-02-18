package com.project.environments.structure;

import java.util.ArrayList;

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

    public String getQualifiedName() {
        String name = "";
        for (String n : fullyQualifiedName) {
            name += n + ".";
        }
        return name.substring(0, name.length() - 1);
    }

    public String getSimpleName() {
        return fullyQualifiedName.get(fullyQualifiedName.size() - 1);
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
}
