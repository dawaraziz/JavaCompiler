package com.project.ParseTree;

import com.project.scanner.Kind;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ParseTree {

    public boolean isTraversed() {
        return traversed;
    }

    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    public String getLexeme() {
        return lexeme;
    }

    Kind kind;

    boolean terminal = false;
    boolean traversed = false;

    ArrayList<ParseTree> children = new ArrayList<>();

    public ParseTree parent;
    public String lexeme;

    public ParseTree(final Kind kind, final boolean terminal, final boolean traversed, final ParseTree parent) {
        this.kind = kind;
        this.terminal = terminal;
        this.traversed = traversed;
        this.parent = parent;
    }

    public ParseTree(final Kind kind, final String lexeme, final boolean terminal, final boolean traversed, final ParseTree parent) {
        this.kind = kind;
        this.lexeme = lexeme;
        this.terminal = terminal;
        this.traversed = traversed;
        this.parent = parent;
    }

    public ParseTree(final String lexeme, final Kind kind) {
        this.kind = kind;
        this.lexeme = lexeme;
        this.terminal = true;
        this.traversed = false;
    }

    public void addChild(Kind value, boolean terminal, boolean traversed, ParseTree parent) {
        this.children.add(new ParseTree(value, terminal, traversed, parent));
    }

    public ArrayList<ParseTree> getChildrenWithLexeme(String lexeme) {
        ArrayList<ParseTree> childrenWithLexeme = new ArrayList<>();

        for (ParseTree child : children) {
            childrenWithLexeme.addAll(child.getChildrenWithLexeme(lexeme));
            if (child.lexeme.equals(lexeme)) {
                childrenWithLexeme.add(child);
            }
        }

        return childrenWithLexeme;
    }

    public ArrayList<ParseTree> getDirectChildrenWithLexeme(String lexeme) {
        ArrayList<ParseTree> childrenWithLexeme = new ArrayList<>();

        for (ParseTree child : children) {
            if (child.lexeme.equals(lexeme)) {
                childrenWithLexeme.add(child);
            }
        }

        return childrenWithLexeme;
    }

    public static ArrayList<String> getStringList(ArrayList<ParseTree> parseTreeList) {
        ArrayList<String> stringList = new ArrayList<>();

        for (ParseTree parseTree : parseTreeList) {
            stringList.add(parseTree.lexeme);
        }

        return stringList;
    }

    public ArrayList<ParseTree> getLeafNodes() {
        ArrayList<ParseTree> leafNodes = new ArrayList<>();

        if (children.isEmpty()) {
            leafNodes.add(this);
        } else {
            for (ParseTree child : children) {
                leafNodes.addAll(child.getLeafNodes());
            }
        }

        return leafNodes;
    }

    public void addChild(ParseTree tree) {
        this.children.add(tree);
    }

    public ParseTree getChild(int i) {
        return this.children.get(i);
    }

    public boolean noChildren() {
        return this.children.isEmpty();
    }

    public ArrayList<ParseTree> getChildren() {
        return this.children;
    }

    public void setTraversed(boolean traversed) {
        this.traversed = traversed;
    }

    public ParseTree getParent() {
        return this.parent;
    }

    public Kind getKind() {
        return this.kind;
    }

    public boolean isTerminal() {
        return this.terminal;
    }

}
