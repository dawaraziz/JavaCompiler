package com.project.ast;

import com.project.ast.structure.CharacterLiteralHolder;
import com.project.ast.structure.IntegerLiteralHolder;
import com.project.ast.structure.StringLiteralHolder;
import com.project.environments.BlockScope;
import com.project.environments.DefinitionScope;
import com.project.environments.JoosClassScope;
import com.project.environments.JoosImportScope;
import com.project.environments.Scope;
import com.project.environments.structure.Name;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;
import com.project.parser.structure.ParserSymbol;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;
import java.util.Scanner;

import static com.project.ast.ASTNode.lexemesToStringList;
import static com.project.ast.structure.IntegerLiteralHolder.ParentType.OTHER;
import static com.project.ast.structure.IntegerLiteralHolder.ParentType.UNARY;
import static com.project.environments.JoosClassScope.CLASS_TYPE;
import static com.project.environments.JoosImportScope.IMPORT_TYPE.ON_DEMAND;
import static com.project.environments.JoosImportScope.IMPORT_TYPE.SINGLE;

public class ASTHead {

    private final static String CLASS_DECLARATION = "CLASSDECLARATION";
    private final static String CONSTRUCTOR_DECLARATION = "CONSTRUCTORDECLARATION";
    private final static String METHOD_DECLARATION = "METHODDECLARATION";
    private final static String METHOD_HEADER = "METHODHEADER";
    private final static String VARIABLE_ID = "VARIABLE_ID";
    private final static String MODIFIERS = "MODIFIERS";
    private final static String MODIFIER = "MODIFIER";
    private final static String INTERFACE_DECLARATION = "INTERFACEDECLARATION";
    private final static String BLOCK = "BLOCK";
    private final static String UNARY_EXPRESSION = "UNARYEXPRESSION";
    private final static String FORMAL_PARAMETER = "FORMALPARAMETER";
    private final static String METHOD_DECLARATOR = "METHODDECLARATOR";
    private final static String INTERFACES = "INTERFACES";
    private final static String INTERFACE_TYPE_LIST = "INTERFACETYPELIST";
    private final static String BLOCK_STATEMENTS = "BLOCKSTATEMENTS";
    private final static String LOCAL_VARIABLE_DECLARATION_STATEMENT = "LOCALVARIABLEDECLARATIONSTATEMENT";

    private final static String SUPER = "SUPER";

    // PACKAGES
    private final static String PACKAGE_DECLARATION = "PACKAGEDECLARATION";
    private final static String QUALIFIED_NAME = "QUALIFIEDNAME";
    private final static String SIMPLE_NAME = "SIMPLENAME";

    // IMPORTS
    private final static String TYPE_IMPORT_ON_DEMAND_DECLARATION = "TYPEIMPORTONDEMANDDECLARATION";
    private final static String SINGLE_TYPE_IMPORT_DECLARATION = "SINGLETYPEIMPORTDECLARATION";

    // MODIFIERS
    public final static String PUBLIC = "public";
    public final static String PROTECTED = "protected";
    public final static String STATIC = "static";
    public final static String ABSTRACT = "abstract";
    public final static String FINAL = "final";
    public final static String NATIVE = "native";

    // FIELDS
    private final static String FIELD_DECLARATION = "FIELDDECLARATION";
    private final static String VARIABLE_DECLARATOR_ID = "VARIABLEDECLARATORID";
    private final static String VARIABLE_DECLARATOR = "VARIABLEDECLARATOR";

    private final static ArrayList<String> safeCull = new ArrayList<>();

    static {
        safeCull.add(SIMPLE_NAME);
    }

    private final ASTNode headNode;

    public ASTHead(final ParserSymbol parseTree) {
        headNode = trimParseTree(parseTree, null);
    }

    private ASTHead(final ASTNode head) {
        headNode = head;
    }

    public Name getPackageName() {
        final ArrayList<ASTNode> packageNodes = headNode.findNodesWithLexeme(PACKAGE_DECLARATION);

        if (packageNodes.size() == 0) {
            return null;
        } else if (packageNodes.size() > 1) {
            System.err.println("Somehow identified more than one package in a class; aborting!");
            System.exit(42);
        }

        return new Name(lexemesToStringList(getNameNode(packageNodes.get(0)).getLeafNodes()));
    }

    public String getFieldName() {
        final ArrayList<ASTNode> nameNodes = headNode.findNodesWithLexeme(VARIABLE_DECLARATOR_ID);

        if (nameNodes.size() != 1) {
            System.err.println("Identified field with incorrect name; aborting!");
            System.exit(42);
        }

        return nameNodes.get(0).children.get(0).lexeme;
    }

    private static ASTNode getNameNode(final ASTNode startNode) {
        ArrayList<ASTNode> nameNodes = startNode
                .getDirectChildrenWithLexemes(QUALIFIED_NAME, SIMPLE_NAME);

        if (nameNodes.size() != 1) {
            nameNodes = startNode.getDirectChildrenWithKinds(VARIABLE_ID);

            if (nameNodes.size() != 1) {
                System.err.println("Identified badly formatted name in a class; aborting!");
                System.exit(42);
            }
        }
        return nameNodes.get(0);
    }

    public String getExpectedFileName() {
        final ArrayList<ASTNode> declarations =
                headNode.findNodesWithLexeme(CLASS_DECLARATION, INTERFACE_DECLARATION);

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

    public ASTHead getClassDeclaration() {
        final ArrayList<ASTNode> declarations = headNode.findNodesWithLexeme(CLASS_DECLARATION, INTERFACE_DECLARATION);

        if (declarations.size() > 1) {
            System.err.println("File has more than one class; aborting!");
            System.exit(42);
        }

        return new ASTHead(declarations.get(0));
    }

    public ArrayList<String> getClassModifiers() {
        final ArrayList<ASTNode> declarations =
                headNode.findNodesWithLexeme(CLASS_DECLARATION, INTERFACE_DECLARATION);

        if (declarations.size() > 1) {
            System.err.println("File has more than one class; aborting!");
            System.exit(42);
        }

        final ArrayList<ASTNode> modifiers = declarations.get(0)
                .getDirectChildrenWithLexemes(MODIFIERS, MODIFIER);

        return lexemesToStringList(modifiers.get(0).getLeafNodes());
    }

    public ArrayList<ArrayList<String>> getFieldModifiers() {
        return getModifiers(headNode.findNodesWithLexeme(FIELD_DECLARATION));
    }

    public ArrayList<ArrayList<String>> getMethodModifiers() {
        return getModifiers(headNode.findNodesWithLexeme(METHOD_HEADER));
    }

    public int getMethodCount() {
        return headNode.findNodesWithLexeme(METHOD_HEADER).size();
    }

    public int getFieldCount() {
        return headNode.findNodesWithLexeme(FIELD_DECLARATION).size();
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

    public ArrayList<IntegerLiteralHolder> getIntegerLiterals() {
        final ArrayList<ASTNode> literalNodes = headNode.findNodesWithKinds(Kind.INTEGER_LITERAL);

        final ArrayList<IntegerLiteralHolder> holders = new ArrayList<>();

        for (final ASTNode literalNode : literalNodes) {

            final Scanner scanner = new Scanner(literalNode.lexeme);

            if (!scanner.hasNextLong()) {
                System.err.println("Encountered non-integer INTEGER_LITERAL: " + literalNode.lexeme);
                System.exit(42);
            }

            final long literal = scanner.nextLong();

            // Hacky and terrible, but that's compilers!
            if (literalNode.parent.parent.lexeme.equals(UNARY_EXPRESSION)) {
                holders.add(new IntegerLiteralHolder(UNARY, literal));
            } else {
                holders.add(new IntegerLiteralHolder(OTHER, literal));
            }
        }

        return holders;
    }

    public CLASS_TYPE getClassType() {
        if (headNode.lexeme.equals(INTERFACE_DECLARATION)) {
            return CLASS_TYPE.INTERFACE;
        } else if (headNode.lexeme.equals(CLASS_DECLARATION)) {
            return CLASS_TYPE.CLASS;
        } else {
            System.err.println("Expected class type, found " + headNode.lexeme + "Aborting!");
            System.exit(42);
        }
        return null;
    }

    public ArrayList<CharacterLiteralHolder> getCharacterLiterals() {
        final ArrayList<ASTNode> literalNodes = headNode.findNodesWithKinds(Kind.CHARACTER_LITERAL);

        final ArrayList<CharacterLiteralHolder> holders = new ArrayList<>();

        for (final ASTNode literalNode : literalNodes) {
            holders.add(new CharacterLiteralHolder(literalNode, literalNode.lexeme));
        }

        return holders;
    }

    public ArrayList<StringLiteralHolder> getStringLiterals() {
        final ArrayList<ASTNode> literalNodes = headNode.findNodesWithKinds(Kind.STRING_LITERAL);

        final ArrayList<StringLiteralHolder> holders = new ArrayList<>();

        for (final ASTNode literalNode : literalNodes) {
            holders.add(new StringLiteralHolder(literalNode, literalNode.lexeme));
        }

        return holders;
    }

    // This method is effectively the constructor.
    // However, since we can't really recurse on the
    // constructor, we have to make this separate method.
    private static ASTNode trimParseTree(ParserSymbol parseTree, final ASTNode parent) {
        if (parseTree.kind == null && parseTree.children.isEmpty()) {
            System.err.println("Got a terminal symbol with no children; aborting!");
            System.exit(42);
        }

        parseTree = cullTransitionalNodes(parseTree);

        final ASTNode node = new ASTNode(parseTree);
        node.parent = parent;

        // Cull any recursively expanding nodes.
        for (int i = 0; i < parseTree.children.size(); ++i) {
            final ParserSymbol child = parseTree.children.get(i);

            if (child.equals(parseTree)) {

                // If we find a child we think could be culled, we have to be very careful.
                // We need to preserve the ordering, so we make a new list, expand the problem
                // symbol at the correct place, and add all the other children back in.
                final ArrayList<ParserSymbol> culledChildren = new ArrayList<>();
                parseTree.children.remove(i);

                for (int j = 0; j < i; ++j) {
                    culledChildren.add(parseTree.children.get(j));
                }

                culledChildren.addAll(child.children);

                for (int j = i; j < parseTree.children.size(); ++j) {
                    culledChildren.add(parseTree.children.get(j));
                }

                parseTree.children = culledChildren;

                return trimParseTree(parseTree, parent);
            }
        }

        // Trim all of our children.
        for (final ParserSymbol child : parseTree.children) {
            node.children.add(trimParseTree(child, node));
        }

        return node;
    }

    private static ParserSymbol cullTransitionalNodes(final ParserSymbol parseTree) {
        if (parseTree.children.size() == 1) {
            final ParserSymbol child = parseTree.children.get(0);

            if (!child.isTerminal() || safeCull.contains(parseTree.lexeme)) {
                return cullTransitionalNodes(child);
            }
        }

        final ParserSymbol newParseTree = new ParserSymbol(parseTree.kind, parseTree.lexeme);
        for (final ParserSymbol child : parseTree.children) {
            newParseTree.children.add(cullTransitionalNodes(child));
        }
        return newParseTree;
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

    public ArrayList<JoosImportScope> getImports(final JoosClassScope parentClass) {
        final ArrayList<JoosImportScope> imports = new ArrayList<>();

        final ArrayList<ASTNode> onDemandImports = headNode.findNodesWithLexeme(TYPE_IMPORT_ON_DEMAND_DECLARATION);

        for (final ASTNode onDemandImport : onDemandImports) {
            final ASTNode name = getNameNode(onDemandImport);

            if (name.kind == Kind.VARIABLE_ID) {
                imports.add(new JoosImportScope(ON_DEMAND, name.lexeme, parentClass));
            } else {
                imports.add(new JoosImportScope(ON_DEMAND, lexemesToStringList(name.children), parentClass));
            }
        }

        final ArrayList<ASTNode> singleImports = headNode.findNodesWithLexeme(SINGLE_TYPE_IMPORT_DECLARATION);

        for (final ASTNode singleImport : singleImports) {
            final ASTNode name = getNameNode(singleImport);

            if (name.kind == Kind.VARIABLE_ID) {
                imports.add(new JoosImportScope(SINGLE, name.lexeme, parentClass));
            } else {
                imports.add(new JoosImportScope(SINGLE, lexemesToStringList(name.children), parentClass));
            }
        }

        return imports;
    }

    public ArrayList<ASTHead> getFieldNodes() {
        final ArrayList<ASTNode> fields = headNode.findNodesWithLexeme(FIELD_DECLARATION);
        final ArrayList<ASTHead> fieldHeads = new ArrayList<>();

        for (final ASTNode field : fields) {
            fieldHeads.add(new ASTHead(field));
        }

        return fieldHeads;
    }

    public ArrayList<ASTHead> getMethodNodes() {
        final ArrayList<ASTNode> methods = headNode.findNodesWithLexeme(METHOD_DECLARATION);
        final ArrayList<ASTHead> methodHeads = new ArrayList<>();

        for (final ASTNode method : methods) {
            methodHeads.add(new ASTHead(method));
        }

        return methodHeads;
    }

    public ASTHead getFieldInitializer() {
        final ArrayList<ASTNode> initializers = headNode.findNodesWithLexeme(VARIABLE_DECLARATOR);

        if (initializers.size() > 1) {
            System.err.println("Found more than one field initializer; aborting!");
            System.exit(42);
        } else if (initializers.size() == 0) {
            return null;
        }

        return new ASTHead(initializers.get(0));
    }

    public Type getFieldType() {
        return new Type(lexemesToStringList(headNode.children.get(2).getLeafNodes()));
    }

    public ASTHead getMethodBlock() {
        final ASTNode head = headNode.children.get(0);

        if (head.lexeme.equals(BLOCK)) {
            return new ASTHead(head);
        } else if (head.children.get(0).lexeme.equals(";")) {
            return null;
        } else {
            System.err.println("Couldn't find method body; aborting!");
            System.exit(42);
        }
        return null;
    }


    public ArrayList<Parameter> getMethodParameters() {
        final ArrayList<Parameter> parameterList = new ArrayList<>();

        final ArrayList<ASTNode> parameters = headNode.findNodesWithLexeme(FORMAL_PARAMETER);

        for (final ASTNode parameter : parameters) {
            final Name name = new Name(lexemesToStringList(parameter.children.get(0).getLeafNodes()));
            final Type type = new Type(lexemesToStringList(parameter.children.get(1).getLeafNodes()));
            parameterList.add(new Parameter(type, name));
        }

        return parameterList;
    }

    public Type getMethodReturnType() {
        final ASTNode header = headNode.findNodesWithLexeme(METHOD_HEADER).get(0);
        return new Type(lexemesToStringList(header.children.get(1).getLeafNodes()));
    }

    public String getMethodName() {
        final ArrayList<ASTNode> nameNodes = headNode.findNodesWithLexeme(METHOD_DECLARATOR);
        if (nameNodes.get(0).children.size() == 3) {
            return nameNodes.get(0).children.get(2).lexeme;
        } else {
            return nameNodes.get(0).children.get(3).lexeme;
        }
    }

    public ArrayList<Name> getClassInterfaces() {
        final ArrayList<ASTNode> nodes = headNode.getDirectChildrenWithLexemes(INTERFACES);

        if (nodes.size() == 0) {
            return null;
        }

        final ASTNode interfaceNode = nodes.get(0).children.get(0);

        final ArrayList<Name> names = new ArrayList<>();
        if (interfaceNode.lexeme.equals(INTERFACE_TYPE_LIST)) {
            for (final ASTNode child : interfaceNode.children) {
                if (child.kind != Kind.COMMA) {
                    names.add(new Name(lexemesToStringList(child.getLeafNodes())));
                }
            }
        } else {
            names.add(new Name(lexemesToStringList(interfaceNode.getLeafNodes())));
        }


        return names;
    }

    public Name getClassSuperClass() {
        final ArrayList<ASTNode> nodes = headNode.getDirectChildrenWithLexemes(SUPER);

        if (nodes.size() == 0) {
            return null;
        }

        return new Name(lexemesToStringList(nodes.get(0).children.get(0).getLeafNodes()));
    }

    public Scope generateMethodScopes(final Scope parentScope) {
        if (headNode.lexeme.equals(BLOCK)) {
            final ASTNode blockBody = headNode.children.get(1);

            final BlockScope scope = new BlockScope(new ASTHead(blockBody), parentScope);

            if (blockBody.lexeme.equals(BLOCK_STATEMENTS)) {
                for (int i = blockBody.children.size() - 1; i >= 0; --i) {
                    final ASTHead statement = new ASTHead(blockBody.children.get(i));

                    final Scope statementScope = statement.generateMethodScopes(scope);

                    if (statementScope == null) {
                        scope.statements.add(statement);
                    } else {
                        scope.childScopes.add(statementScope);
                        if (statementScope instanceof DefinitionScope) {
                            break;
                        }
                    }
                }
            } else if (blockBody.kind == null) {
                final ASTHead statement = new ASTHead(blockBody);
                final Scope statementScope = statement.generateMethodScopes(scope);

                if (statementScope == null) {
                    scope.statements.add(statement);
                } else {
                    scope.childScopes.add(statementScope);
                }
            }

            return scope;
        } else if (headNode.lexeme.equals(LOCAL_VARIABLE_DECLARATION_STATEMENT)) {
            final ASTNode declaration = headNode.children.get(1);

            final Type type = new Type(lexemesToStringList(declaration.children.get(1).getLeafNodes()));
            final String name = declaration.findNodesWithLexeme(VARIABLE_DECLARATOR_ID).get(0).children.get(0).lexeme;
            ASTHead initialization = null;
            if (declaration.children.get(0).children.size() == 3) {
                initialization = new ASTHead(declaration.children.get(0).children.get(0));
            }

            final DefinitionScope scope = new DefinitionScope(new ASTHead(declaration),
                    parentScope, type, name, initialization);

            return scope;
        }

        return null;
    }
}
