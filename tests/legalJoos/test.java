package tests.legalJoos;

import com.project.scanner.Kind;

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

    ArrayList children = new ArrayList();

    public ParseTree parent;
    public String lexeme;

    public ParseTree( Kind kind,  boolean terminal,  boolean traversed,  ParseTree parent) {
        this.kind = kind;
        this.terminal = terminal;
        this.traversed = traversed;
        this.parent = parent;
    }

    public ParseTree( Kind kind,  String lexeme,  boolean terminal,  boolean traversed,  ParseTree parent) {
        this.kind = kind;
        this.lexeme = lexeme;
        this.terminal = terminal;
        this.traversed = traversed;
        this.parent = parent;
    }

    public ParseTree( String lexeme,  Kind kind) {
        this.kind = kind;
        this.lexeme = lexeme;
        this.terminal = true;
        this.traversed = false;
    }

    public void addChild(Kind value, boolean terminal, boolean traversed, ParseTree parent) {
        this.children.add(new ParseTree(value, terminal, traversed, parent));
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

    public ArrayList getChildren() {
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