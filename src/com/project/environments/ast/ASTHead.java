package com.project.environments.ast;

import com.project.environments.ast.structure.CharacterLiteralHolder;
import com.project.environments.ast.structure.IntegerLiteralHolder;
import com.project.environments.ast.structure.StringLiteralHolder;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.ImportScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Name;
import com.project.environments.structure.Parameter;
import com.project.environments.structure.Type;
import com.project.parser.structure.ParserSymbol;
import com.project.scanner.structure.Kind;
import resources.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Stack;
import java.util.stream.Collectors;

import static com.project.environments.ast.ASTNode.lexemesToStringList;
import static com.project.environments.ast.structure.IntegerLiteralHolder.ParentType.OTHER;
import static com.project.environments.ast.structure.IntegerLiteralHolder.ParentType.UNARY;
import static com.project.environments.scopes.ClassScope.CLASS_TYPE;
import static com.project.environments.scopes.ImportScope.IMPORT_TYPE.ON_DEMAND;
import static com.project.environments.scopes.ImportScope.IMPORT_TYPE.SINGLE;
import static com.project.scanner.structure.Kind.AMBIGUOUSNAME;
import static com.project.scanner.structure.Kind.CURLY_BRACKET_CLOSE;
import static com.project.scanner.structure.Kind.CURLY_BRACKET_OPEN;
import static com.project.scanner.structure.Kind.EXPRESSIONNAME;
import static com.project.scanner.structure.Kind.METHODNAME;
import static com.project.scanner.structure.Kind.PACKAGENAME;
import static com.project.scanner.structure.Kind.PACKAGEORTYPENAME;
import static com.project.scanner.structure.Kind.TYPENAME;

public class ASTHead {

    // GENERAL LEXEMES
    private final static String BLOCK = "BLOCK";
    private final static String VARIABLE_ID = "VARIABLE_ID";
    private final static String QUALIFIED_NAME = "QUALIFIEDNAME";
    private final static String SIMPLE_NAME = "SIMPLENAME";
    private final static String SEMICOLON = ";";

    // MODIFIERS
    private final static String MODIFIERS = "MODIFIERS";
    private final static String MODIFIER = "MODIFIER";
    public final static String PUBLIC = "public";
    public final static String PROTECTED = "protected";
    public final static String STATIC = "static";
    public final static String ABSTRACT = "abstract";
    public final static String FINAL = "final";
    public final static String NATIVE = "native";

    // CLASS LEXEMES
    private final static String CONSTRUCTOR_BODY = "CONSTRUCTORBODY";
    private final static String CLASS_DECLARATION = "CLASSDECLARATION";
    private final static String INTERFACE_DECLARATION = "INTERFACEDECLARATION";
    private final static String INTERFACES = "INTERFACES";
    private final static String INTERFACE_TYPE_LIST = "INTERFACETYPELIST";
    private final static String EXTENDS_INTERFACES = "EXTENDSINTERFACES";
    private final static String SUPER = "SUPER";

    // METHOD LEXEMES
    private final static String CONSTRUCTOR_DECLARATION = "CONSTRUCTORDECLARATION";
    private final static String CONSTRUCTOR_DECLARATOR = "CONSTRUCTORDECLARATOR";
    private final static String METHOD_DECLARATION = "METHODDECLARATION";
    private final static String ABSTRACT_METHOD_DECLARATION = "ABSTRACTMETHODDECLARATION";
    private final static String METHOD_DECLARATOR = "METHODDECLARATOR";
    private final static String METHOD_HEADER = "METHODHEADER";
    private final static String FORMAL_PARAMETER = "FORMALPARAMETER";

    // FIELDS
    private final static String FIELD_DECLARATION = "FIELDDECLARATION";
    public final static String VARIABLE_DECLARATOR_ID = "VARIABLEDECLARATORID";
    private final static String VARIABLE_DECLARATOR = "VARIABLEDECLARATOR";

    // EXPRESSION LEXEMES
    private final static String UNARY_EXPRESSION = "UNARYEXPRESSION";
    private final static String CAST_EXPRESSION = "CASTEXPRESSION";
    private final static String EXPRESSION = "EXPRESSION";
    private final static String ADDITIVE_EXPRESSION = "ADDITIVEEXPRESSION";
    private final static String UNARY_EXPRESSION_NOT_PLUS_MINUS = "UNARYEXPRESSIONNOTPLUSMINUS";
    private final static String PRIMARY_NO_NEW_ARRAY = "PRIMARYNONEWARRAY";
    private final static String CLASS_INSTANCE_CREATION_EXPRESSION = "CLASSINSTANCECREATIONEXPRESSION";

    // STATEMENT LEXEMES
    public final static String LOCAL_VARIABLE_DECLARATION_STATEMENT = "LOCALVARIABLEDECLARATIONSTATEMENT";
    private final static String LOCAL_VARIABLE_DECLARATION = "LOCALVARIABLEDECLARATION";

    private final static String IF_THEN_STATEMENT = "IFTHENSTATEMENT";
    private final static String IF_THEN_ELSE_STATEMENT = "IFTHENELSESTATEMENT";
    private final static String IF_THEN_ELSE_STATEMENT_NO_SHORT_IF = "IFTHENELSESTATEMENTNOSHORTIF";

    private final static String WHILE_STATEMENT = "WHILESTATEMENT";
    private final static String WHILE_STATEMENT_NO_SHORT_IF = "WHILESTATEMENTNOSHORTIF";

    private final static String FOR_STATEMENT = "FORSTATEMENT";
    private final static String FOR_STATEMENT_NO_SHORT_IF = "FORSTATEMENTNOSHORTIF";

    // PACKAGE LEXEMES
    private final static String PACKAGE_DECLARATION = "PACKAGEDECLARATION";

    // IMPORTS
    private final static String TYPE_IMPORT_ON_DEMAND_DECLARATION = "TYPEIMPORTONDEMANDDECLARATION";
    private final static String SINGLE_TYPE_IMPORT_DECLARATION = "SINGLETYPEIMPORTDECLARATION";


    private final static ArrayList<String> safeCull;

    static {
        safeCull = new ArrayList<>();
        safeCull.add(SIMPLE_NAME);
    }

    private final ASTNode headNode;


    // CONSTRUCTORS
    public ASTHead(final ParserSymbol parseTree) {
        headNode = trimParseTree(parseTree, null);
    }

    public ASTHead(final ASTNode head) {
        headNode = head;
    }

    public void printAST() {
        final Stack<Pair<ASTNode, Integer>> stack = new Stack<>();
        stack.add(new Pair<>(headNode, 0));
        while (!stack.empty()) {
            final Pair<ASTNode, Integer> pair = stack.pop();
            final ASTNode curr = pair.getO1();
            final int level = pair.getO2();
            for (int i = 0; i < level; i++) {
                System.out.print("\t");
            }
            System.out.println(curr.lexeme + " : " + curr.kind + " : " + level);
            for (final ASTNode child : curr.children) {
                stack.add(new Pair<>(child, level + 1));
            }
        }
    }

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
        if (parseTree.lexeme.equals(CAST_EXPRESSION)
                && parseTree.children.get(2).lexeme.equals(EXPRESSION)
                && !parseTree.children.get(2).isVariableCullTree(Kind.VARIABLE_ID)) {
            System.err.println("Code has invalid cast statement.");
            System.exit(42);
        }

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

    // FILE FUNCTIONS
    public ArrayList<ImportScope> getImports(final ClassScope parentClass) {
        final ArrayList<ImportScope> imports = new ArrayList<>();

        final ArrayList<ASTNode> onDemandImports = headNode.findNodesWithLexeme(TYPE_IMPORT_ON_DEMAND_DECLARATION);

        for (final ASTNode onDemandImport : onDemandImports) {
            final ASTNode name = getNameNode(onDemandImport);

            if (name.kind == Kind.PACKAGEORTYPENAME || name.kind == Kind.TYPENAME) {
                imports.add(new ImportScope(ON_DEMAND, name.lexeme, parentClass));
            } else {
                imports.add(new ImportScope(ON_DEMAND, lexemesToStringList(name.children), parentClass));
            }
        }

        final ArrayList<ASTNode> singleImports = headNode.findNodesWithLexeme(SINGLE_TYPE_IMPORT_DECLARATION);

        for (final ASTNode singleImport : singleImports) {
            final ASTNode name = getNameNode(singleImport);

            if (name.kind == Kind.PACKAGEORTYPENAME) {
                imports.add(new ImportScope(SINGLE, name.lexeme, parentClass));
            } else {
                imports.add(new ImportScope(SINGLE, lexemesToStringList(name.children), parentClass));
            }
        }

        imports.add(new ImportScope(ON_DEMAND, Name.generateLangImportName(), parentClass));
        return imports;
    }

    public HashSet<String> getUsedTypeNames() {
        final HashSet<String> ret = new HashSet<>();

        if (headNode.withinLexeme("CLASSBODY")) {
            // If qualified name with first child a typename (last part of qualified name)
            if (headNode.lexeme.equals("QUALIFIEDNAME")
                    && headNode.children.get(0).kind == Kind.TYPENAME) {
                ret.add(headNode.stringFromChildren());
            }
            if (headNode.kind == Kind.TYPENAME
                    && !headNode.withinLexeme("QUALIFIEDNAME")) {
                ret.add(headNode.lexeme);
            }
        }

        getChildren().forEach(c -> ret.addAll(c.getUsedTypeNames()));

        return ret;
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

    public boolean isFileTypeInterface() {
        return !headNode.findNodesWithLexeme(INTERFACE_DECLARATION).isEmpty();
    }


    // CLASS FUNCTIONS
    public ASTHead getClassDeclaration() {
        final ArrayList<ASTNode> declarations = headNode.findNodesWithLexeme(CLASS_DECLARATION, INTERFACE_DECLARATION);

        if (declarations.size() > 1) {
            System.err.println("File has more than one class; aborting!");
            System.exit(42);
        }

        return new ASTHead(declarations.get(0));
    }

    public Name getPackageName() {
        final ArrayList<ASTNode> packageNodes = headNode.findNodesWithLexeme(PACKAGE_DECLARATION);

        if (packageNodes.size() == 0) {
            return new Name("default___");
        } else if (packageNodes.size() > 1) {
            System.err.println("Somehow identified more than one package in a class; aborting!");
            System.exit(42);
        }

        return new Name(lexemesToStringList(getNameNode(packageNodes.get(0)).getLeafNodes()));
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

    public ArrayList<Name> getClassSuperClass() {
        final ArrayList<ASTNode> nodes = headNode.getDirectChildrenWithLexemes(SUPER);

        if (nodes.size() == 0) {
            return null;
        }

        final ArrayList<Name> extendsName = new ArrayList<>();
        extendsName.add(new Name(lexemesToStringList(nodes.get(0).children.get(0).getLeafNodes())));
        return extendsName;
    }

    public ArrayList<Name> getInterfaceSuperInterfaces() {
        final ArrayList<ASTNode> nodes = headNode.getDirectChildrenWithLexemes(EXTENDS_INTERFACES);

        if (nodes.size() == 0) {
            return null;
        }

        final ArrayList<Name> extendsName = new ArrayList<>();
        for (final ASTNode node : nodes.get(0).children) {
            if (node.kind == null || node.kind == Kind.TYPENAME) {
                extendsName.add(new Name(lexemesToStringList(node.getLeafNodes())));
            }
        }
        return extendsName;
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


    // FIELD FUNCTIONS
    public String getFieldName() {
        final ArrayList<ASTNode> nameNodes = headNode.findNodesWithLexeme(VARIABLE_DECLARATOR_ID);

        if (nameNodes.size() != 1) {
            System.err.println("Identified field with incorrect name; aborting!");
            System.exit(42);
        }

        return nameNodes.get(0).children.get(0).lexeme;
    }

    public ArrayList<ArrayList<String>> getAllFieldModifiers() {
        return getModifiers(headNode.findNodesWithLexeme(FIELD_DECLARATION));
    }

    public ArrayList<String> getFieldModifiers() {
        final ArrayList<ArrayList<String>> modifiers = getModifiers(headNode.findNodesWithLexeme(FIELD_DECLARATION));

        if (modifiers.size() != 1) {
            System.err.println("Encountered field with incorrect modifiers; Aborting!");
            System.exit(42);
        }

        return modifiers.get(0);
    }

    public int getFieldCount() {
        return headNode.findNodesWithLexeme(FIELD_DECLARATION).size();
    }

    public ArrayList<ASTHead> getFieldNodes() {
        final ArrayList<ASTNode> fields = headNode.findNodesWithLexeme(FIELD_DECLARATION);
        final ArrayList<ASTHead> fieldHeads = new ArrayList<>();

        for (final ASTNode field : fields) {
            fieldHeads.add(new ASTHead(field));
        }

        return fieldHeads;
    }

    public ASTHead getFieldInitializer() {
        final ArrayList<ASTNode> initializers = headNode.findNodesWithLexeme(VARIABLE_DECLARATOR);

        if (initializers.size() > 1) {
            System.err.println("Found more than one field initializer; aborting!");
            System.exit(42);
        } else if (initializers.size() == 0) {
            return null;
        }

        final ASTHead initializer = new ASTHead(initializers.get(0));

        if (initializer.getChildren().size() == 3) {
            return initializer.getChild(0);
        } else {
            return null;
        }
    }

    public Type getFieldType() {
        return new Type(lexemesToStringList(headNode.children.get(2).getLeafNodes()));
    }


    // METHOD FUNCTIONS
    public ArrayList<ArrayList<String>> getMethodModifiers() {
        return getModifiers(headNode.findNodesWithLexeme(METHOD_HEADER));
    }

    public int getMethodCount() {
        return headNode.findNodesWithLexeme(METHOD_HEADER).size();
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

    public ASTHead getConstructorBlock() {
        return new ASTHead(headNode.children.get(0));
    }

    public ArrayList<ArrayList<String>> getConstructorModifiers() {
        return getModifiers(headNode.findNodesWithLexeme(CONSTRUCTOR_DECLARATION));
    }

    public ArrayList<ASTHead> getMethodNodes() {
        final ArrayList<ASTNode> methods = headNode.findNodesWithLexeme(METHOD_DECLARATION, ABSTRACT_METHOD_DECLARATION);
        final ArrayList<ASTHead> methodHeads = new ArrayList<>();

        for (final ASTNode method : methods) {
            methodHeads.add(new ASTHead(method));
        }

        return methodHeads;
    }

    public ASTHead getMethodBlock() {
        final ASTNode head = headNode.children.get(0);

        if (head.lexeme.equals(BLOCK)) {
            return new ASTHead(head);
        } else if (head.lexeme.equals(SEMICOLON) || head.children.get(0).lexeme.equals(SEMICOLON)) {
            return null;
        } else {
            System.err.println("Couldn't find method body; aborting!");
            System.exit(42);
        }
        return null;
    }

    public ArrayList<Parameter> getMethodParameters(final Scope scope) {
        final ArrayList<Parameter> parameterList = new ArrayList<>();

        final ArrayList<ASTNode> parameters = headNode.findNodesWithLexeme(FORMAL_PARAMETER);

        if (parameters.size() == 0) return null;

        for (final ASTNode parameter : parameters) {
            final Name name = new Name(lexemesToStringList(parameter.children.get(0).getLeafNodes()));
            final Type type = new Type(lexemesToStringList(parameter.children.get(1).getLeafNodes()));
            parameterList.add(new Parameter(type, name, scope));
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

    public String getConstructorName() {
        final ArrayList<ASTNode> nameNodes = headNode.findNodesWithLexeme(CONSTRUCTOR_DECLARATOR);
        if (nameNodes.get(0).children.size() == 3) {
            return nameNodes.get(0).children.get(2).lexeme;
        } else {
            return nameNodes.get(0).children.get(3).lexeme;
        }
    }

    public ArrayList<ASTHead> getConstructorNodes() {
        return headNode.findNodesWithLexeme(CONSTRUCTOR_DECLARATION)
                .stream()
                .map(ASTHead::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<ASTNode> getRHSExpressionNames() {
        ArrayList<ASTNode> RHSName = null;
        final ASTNode varDec = headNode.findFirstDirectChildNodeWithLexeme(VARIABLE_DECLARATOR);
        if (varDec != null) {
            RHSName = varDec.findChildKindsAfterNodeWithKind(Kind.EXPRESSIONNAME, Kind.EQUAL);
        }
        return RHSName;
    }


    // HELPER FUNCTIONS
    private static ASTNode getNameNode(final ASTNode startNode) {
        ArrayList<ASTNode> nameNodes = startNode
                .getDirectChildrenWithLexemes(QUALIFIED_NAME, SIMPLE_NAME);

        // After I change the type from Variable_ID this will break
        if (nameNodes.size() != 1) {
            nameNodes = startNode.getDirectChildrenWithKinds("PACKAGEORTYPENAME", "TYPENAME", "AMBIGUOUSNAME");

            if (nameNodes.size() != 1) {
                System.err.println("Identified badly formatted name in a class; aborting!");
                System.exit(42);
            }
        }
        return nameNodes.get(0);
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

    public ASTNode unsafeGetHeadNode() {
        return headNode;
    }

    // SCOPE FUNCTIONS
    public boolean isBlock() {
        return headNode.lexeme.equals(BLOCK) || headNode.lexeme.equals(CONSTRUCTOR_BODY);
    }

    public boolean isDefinition() {
        return headNode.lexeme.equals(LOCAL_VARIABLE_DECLARATION_STATEMENT)
                || headNode.lexeme.equals(LOCAL_VARIABLE_DECLARATION);
    }

    public boolean isIfStatement() {
        return headNode.lexeme.equals(IF_THEN_STATEMENT)
                || headNode.lexeme.equals(IF_THEN_ELSE_STATEMENT)
                || headNode.lexeme.equals(IF_THEN_ELSE_STATEMENT_NO_SHORT_IF);
    }

    public boolean isWhileStatement() {
        return headNode.lexeme.equals(WHILE_STATEMENT)
                || headNode.lexeme.equals(WHILE_STATEMENT_NO_SHORT_IF);
    }

    public boolean isForStatement() {
        return headNode.lexeme.equals(FOR_STATEMENT)
                || headNode.lexeme.equals(FOR_STATEMENT_NO_SHORT_IF);
    }

    public boolean isAdditiveExpr() {
        return headNode.lexeme.equals(ADDITIVE_EXPRESSION);
    }

    public boolean isCastExpr() {
        return headNode.lexeme.equals(CAST_EXPRESSION);
    }

    public boolean isUnaryExpr() {
        return headNode.lexeme.equals(UNARY_EXPRESSION);
    }

    public boolean isUnaryNotPlusMinusExpr() {
        return headNode.lexeme.equals(UNARY_EXPRESSION_NOT_PLUS_MINUS);
    }

    public boolean isPrimaryNoNewArrayExpr() {
        return headNode.lexeme.equals(PRIMARY_NO_NEW_ARRAY);
    }

    public ArrayList<ASTNode> getVariableIDNodes() {
        return headNode.findNodesWithKinds(Kind.VARIABLE_ID);
    }

    public ArrayList<ASTNode> getQualifiedNameNodes() {
        return headNode.findNodesWithLexeme("QUALIFIEDNAME");
    }

    public boolean isEmptyStatement() {
        return headNode.lexeme.equals("EMPTYSTATEMENT");
    }

    public boolean isReturnStatement() {
        return headNode.lexeme.equals("RETURNSTATEMENT");
    }

    public boolean isExpressionStatement() {
        return headNode.lexeme.equals("EXPRESSIONSTATEMENT");
    }

    public ASTHead getChild(final int index) {
        if (index >= headNode.children.size()) {
            System.err.println("Requesting node child beyond index; aborting!");
            System.exit(42);
        }

        return new ASTHead(headNode.children.get(index));
    }

    public ArrayList<ASTHead> getChildren() {
        return headNode.children.stream()
                .map(ASTHead::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public String getLexeme() {
        return headNode.lexeme;
    }

    public Kind getKind() {
        return headNode.kind;
    }

    public void stripBracesFromBlock() {
        if (headNode.children.size() > 3
                && headNode.children.get(0).kind == CURLY_BRACKET_CLOSE
                && headNode.children.get(headNode.children.size() - 1).kind == CURLY_BRACKET_OPEN) {
            headNode.children.remove(0);
            headNode.children.remove(headNode.children.size() - 1);
            stripBracesFromBlock();
        }
    }

    public boolean isNameExpr() {
        String x = "2";
        return headNode.kind == EXPRESSIONNAME
                || headNode.kind == TYPENAME
                || headNode.kind == PACKAGEORTYPENAME
                || headNode.kind == AMBIGUOUSNAME
                || headNode.kind == PACKAGENAME
                || headNode.kind == METHODNAME
                || headNode.lexeme.equals("QUALIFIEDNAME");
    }


    public boolean isArrayAccessExpression() {
        return headNode.lexeme.equals("ARRAYACCESS");
    }

    public void classifyAsExpressionName() {
        if (headNode.kind != AMBIGUOUSNAME) {
            System.err.println("Trying to reclassify non-ambigious name; aborting!");
            System.exit(42);
        }

        headNode.kind = EXPRESSIONNAME;
    }

    public void classifyAsTypeName() {
        if (headNode.kind != AMBIGUOUSNAME) {
            System.err.println("Trying to reclassify non-ambigious name; aborting!");
            System.exit(42);
        }

        headNode.kind = TYPENAME;
    }

    public void classifyAsPackageName() {
        if (headNode.kind != AMBIGUOUSNAME) {
            System.err.println("Trying to reclassify non-ambigious name; aborting!");
            System.exit(42);
        }

        headNode.kind = PACKAGENAME;
    }

    public ASTHead getLeftmostChild() {
        return getChild(headNode.children.size() - 1);
    }

    public boolean isLiteralExpr() {
        return headNode.lexeme.equals("LITERAL");
    }

    public boolean isTypeExpr() {
        return headNode.lexeme.equals("INTEGRALTYPE")
                || headNode.lexeme.equals("PRIMITIVETYPE");
    }

    public boolean isMethodInvocationExpr() {
        return headNode.lexeme.equals("METHODINVOCATION");
    }

    public boolean isArrayCreationExpr() {
        return headNode.lexeme.equals("ARRAYCREATIONEXPRESSION");
    }

    public boolean isClassInstanceCreationExpr() {
        return headNode.lexeme.equals(CLASS_INSTANCE_CREATION_EXPRESSION);
    }

    public boolean isArrayTypeExpr() {
        return headNode.lexeme.equals("ARRAYTYPE");
    }

    public ASTHead generateMethodSubHead() {
        final ASTNode replacementNode = new ASTNode(null, "METHODINVOCATION");
        replacementNode.children.addAll(headNode.children.subList(1, headNode.children.size() - 2));
        return new ASTHead(replacementNode);
    }

    public ASTHead generatePrimaryMethodSubHead() {
        final ASTNode replacementNode = new ASTNode(null, "METHODINVOCATION");
        replacementNode.children.addAll(headNode.children.subList(1, headNode.children.size() - 4));
        return new ASTHead(replacementNode);
    }

    public ASTHead generateIfSubHead() {
        final ASTNode replacementNode = new ASTNode(null, "IFTHENELSESTATEMENT");
        replacementNode.children.addAll(headNode.children.subList(0, headNode.children.size() - 6));
        return new ASTHead(replacementNode);
    }

    public ASTHead generateBaseSubHead() {
        final ASTNode replacementNode = new ASTNode(null, "SUBBASEEXPRESSION");
        replacementNode.children.addAll(headNode.children.subList(0, headNode.children.size() - 2));
        return new ASTHead(replacementNode);
    }

    public ASTHead generateNameSubHead() {
        final ASTNode replacementNode = new ASTNode(null, "SUBQUALIFIEDNAME");
        replacementNode.children.addAll(headNode.children.subList(2, headNode.children.size()));
        return new ASTHead(replacementNode);
    }

    public ASTHead generateCastSubHead(final boolean isArray) {
        final ASTNode replacementNode = new ASTNode(null, "CASTEXPRESSION");
        if (isArray) {
            replacementNode.children.addAll(headNode.children.subList(0, headNode.children.size() - 4));
        } else {
            replacementNode.children.addAll(headNode.children.subList(0, headNode.children.size() - 3));
        }
        return new ASTHead(replacementNode);
    }

    public ASTHead generateSubHead(final int inclusiveStart,
                                   final int exclusiveEnd,
                                   final String lexeme) {
        final ASTNode replacementNode = new ASTNode(null, lexeme);
        replacementNode.children.addAll(headNode.children.subList(inclusiveStart, exclusiveEnd));
        return new ASTHead(replacementNode);
    }

    public ASTHead generateClassInstanceSubHead() {
        final ASTNode replacementNode = new ASTNode(null, "CLASSINSTANCECREATIONEXPRESSION");
        replacementNode.children.addAll(headNode.children.subList(1, headNode.children.size() - 3));
        return new ASTHead(replacementNode);
    }

    public boolean isArgumentListExpr() {
        return headNode.lexeme.equals("ARGUMENTLIST");
    }

    public boolean isFieldAccessExpr() {
        return headNode.lexeme.equals("FIELDACCESS");
    }

    public boolean isAssignmentExpr() {
        return headNode.lexeme.equals("ASSIGNMENT");
    }
}
