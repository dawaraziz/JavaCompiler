package com.project.Parser;

import com.project.scanner.Token;

import java.util.ArrayList;

public class ParsTree {

    String value = "";

    boolean terminal = false;
    boolean traversed = false;

    ArrayList<ParsTree> children = new ArrayList<>();

    ParsTree parent;

    public ParsTree(final String value, final boolean terminal, final boolean traversed, final ParsTree parent) {
        this.value = value;
        this.terminal = terminal;
        this.traversed = traversed;
        this.parent = parent;
    }

    public void addChild(String value, boolean terminal, boolean traversed, ParsTree parent) {
        this.children.add(new ParsTree(value, terminal, traversed, parent));
    }

    public ParsTree getChild(int i) {
        return this.children.get(i);
    }

    public boolean noChildren() {
        return this.children.isEmpty();
    }

    public ArrayList<ParsTree> getChildren() {
        return this.children;
    }

    public void setTraversed(boolean traversed) {
        this.traversed = traversed;
    }

    public ParsTree getParent() {
        return this.parent;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isTerminal() {
        return this.terminal;
    }

}
