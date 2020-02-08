package com.project.Parser;

import com.project.scanner.Token;

import java.util.ArrayList;

public class ParsTree {

    String value = "";

    boolean terminal = false;

    ArrayList<ParsTree> children = new ArrayList<>();

    ParsTree(final String value, final boolean terminal) {
        this.value = value;
        this.terminal = terminal;
    }

    public void addChild(String value, boolean terminal) {
        this.children.add(new ParsTree(value, terminal));
    }

}
