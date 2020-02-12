package com.project.AST;

import com.project.parser.structure.ParserSymbol;

import java.util.ArrayList;

import static com.project.AST.ASTNode.lexemesToStringList;

public class ASTHead {

    private final static String CLASS_DECLARATIONS = "CLASSDECLARATION";
    private final static String INTERFACE_DECLARATIONS = "INTERFACEDECLARATION";
    private final static String FIELD_DECLARATIONS = "FIELDDECLARATION";
    private final static String CONSTRUCTOR_DECLARATION = "CONSTRUCTORDECLARATION";
    private final static String METHOD_DECLARATION = "METHODDECLARATION";
    private final static String METHOD_HEADER = "METHODHEADER";
    private final static String VARIABLE_ID = "VARIABLE_ID";
    private final static String MODIFIERS = "MODIFIERS";
    private final static String MODIFIER = "MODIFIER";
    private final static String INTERFACE_DECLARATION = "INTERFACEDECLARATION";
    private final static String BLOCK = "BLOCK";

    // MODIFIERS
    public final static String PUBLIC = "public";
    public final static String PROTECTED = "protected";
    public final static String STATIC = "static";
    public final static String ABSTRACT = "abstract";
    public final static String FINAL = "final";
    public final static String NATIVE = "native";

    private final ASTNode headNode;

    public ASTHead(final ParserSymbol parseTree) {
        headNode = trimParseTree(parseTree, null);
    }

    public String getExpectedFileName() {
        final ArrayList<ASTNode> declarations =
                headNode.findNodesWithLexeme(CLASS_DECLARATIONS, INTERFACE_DECLARATIONS);

        if (declarations.size() > 1) {
            System.err.println("File has more than one class; aborting!");
            System.exit(42);
        }

        final ArrayList<ASTNode> names = declarations.get(0).getDirectChildrenWithKinds(VARIABLE_ID);

        if (names.size() > 1) {
            System.err.println("File has more than one class; aborting!");
            System.exit(42);
        }

        return names.get(0).lexeme;
    }

    public ArrayList<String> getClassModifiers() {
        final ArrayList<ASTNode> declarations =
                headNode.findNodesWithLexeme(CLASS_DECLARATIONS, INTERFACE_DECLARATIONS);

        if (declarations.size() > 1) {
            System.err.println("File has more than one class; aborting!");
            System.exit(42);
        }

        final ArrayList<ASTNode> modifiers = declarations.get(0)
                .getDirectChildrenWithLexemes(MODIFIERS, MODIFIER);

        return lexemesToStringList(modifiers.get(0).getLeafNodes());
    }

    public ArrayList<ArrayList<String>> getFieldModifiers() {
        return getModifiers(headNode.findNodesWithLexeme(FIELD_DECLARATIONS));
    }

    public ArrayList<ArrayList<String>> getMethodModifiers() {
        return getModifiers(headNode.findNodesWithLexeme(METHOD_HEADER));
    }

    public int getMethodCount() {
        return headNode.findNodesWithLexeme(METHOD_HEADER).size();
    }

    public int getFieldCount() {
        return headNode.findNodesWithLexeme(FIELD_DECLARATIONS).size();
    }

    public ArrayList<ArrayList<String>> getConstructorModifiers() {
        return getModifiers(headNode.findNodesWithLexeme(CONSTRUCTOR_DECLARATION));
    }

    public boolean isFileTypeInterface() {
        return !headNode.findNodesWithLexeme(INTERFACE_DECLARATION).isEmpty();
    }

    public boolean isAbstractMethodInitialized() {
        final ArrayList<ASTNode> declarations = headNode.findNodesWithLexeme(METHOD_DECLARATION);

        for (final ASTNode declaration : declarations) {
            if (!declaration.findNodesWithLexeme(ABSTRACT).isEmpty()
                    && !declaration.findNodesWithLexeme(BLOCK).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // This method is effectively the constructor.
    // However, since we can't really recurse on the
    // constructor, we have to make this separate method.
    private static ASTNode trimParseTree(ParserSymbol parseTree, final ASTNode parent) {
        if (parseTree.kind == null && parseTree.children.isEmpty()) {
            System.err.println("Got a terminal symbol with no children; aborting!");
            System.exit(42);
        }

        // Cull any transitional nodes.
        // If we see a single non-terminal followed by a terminal, we don't
        // want to cull the non-terminal, as we may be losing information.
        while (parseTree.children.size() == 1 && parseTree.children.get(0).children.size() != 0) {
            parseTree = parseTree.children.get(0);
        }

        final ASTNode node = new ASTNode(parseTree);
        node.parent = parent;

        // Cull any recursively expanding nodes.
        for (boolean culled = false; ; culled = false) {

            for (int i = 0; i < parseTree.children.size(); ++i) {
                final ParserSymbol child = parseTree.children.get(i);
                if (child.equals(parseTree)) {

                    // If we find a child we think could be culled, we have to be very careful.
                    // We need to preserve the ordering, so we make a new list, expand the problem
                    // symbol at the correct place, and add all the other children back in.
                    final ArrayList<ParserSymbol> culledChildren = new ArrayList<>();
                    //noinspection SuspiciousListRemoveInLoop
                    parseTree.children.remove(i);

                    for (int j = 0; j < i; ++j) {
                        culledChildren.add(parseTree.children.get(j));
                    }

                    culledChildren.addAll(child.children);

                    for (int j = i; j < parseTree.children.size(); ++j) {
                        culledChildren.add(parseTree.children.get(j));
                    }

                    culled = true;

                    parseTree.children = culledChildren;
                }
            }

            // We want to do this as many times as required, so we need to
            // check the exit condition based on whether we've culled or not.
            // We can't move this into the for because it assigns culled first.
            if (!culled) break;
        }

        // Trim all of our children.
        for (final ParserSymbol child : parseTree.children) {
            node.children.add(trimParseTree(child, node));
        }

        return node;
    }

    private ArrayList<ArrayList<String>> getModifiers(final ArrayList<ASTNode> declarations) {
        final ArrayList<ArrayList<String>> modifiers = new ArrayList<>();
        for (final ASTNode field : declarations) {
            final ArrayList<ASTNode> modifierNodes = field.getDirectChildrenWithLexemes(MODIFIERS, MODIFIER);
            for (final ASTNode modifierNode : modifierNodes) {
                final ArrayList<ASTNode> modifierTerminals = modifierNode.getLeafNodes();
                modifiers.add(lexemesToStringList(modifierTerminals));
            }
        }

        return modifiers;
    }
}
