package com.project;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.MethodScope;
import com.project.environments.scopes.PackageScope;
import com.project.hierarchy.HierarchyChecker;
import com.project.linker.TypeLinker;
import com.project.parser.JavaParser;
import com.project.parser.structure.ParserSymbol;
import com.project.scanner.JavaScanner;
import com.project.weeders.AbstractMethodWeeder;
import com.project.weeders.ClassModifierWeeder;
import com.project.weeders.ClassNameWeeder;
import com.project.weeders.FieldModifierWeeder;
import com.project.weeders.LiteralWeeder;
import com.project.weeders.MethodModifierWeeder;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.project.environments.scopes.ClassScope.CLASS_TYPE.INTERFACE;

public class Main {

    public static ArrayList<ClassScope> classTable = new ArrayList<>();
    public static HashSet<MethodScope> interfaceSignatureSet = new HashSet<>();

    public static void main(final String[] args) {

        if (args.length < 1) {
            System.err.println("No argument passed; expected file name.");
            System.exit(42);
        }

        for (final String fileName : args) {
            System.out.println("Scanning " + fileName + ".");

            // Scanning step.
            final ArrayList<ParserSymbol> tokens = JavaScanner.tokenizeFile(fileName);

            InputStream inputStreamJLR1 = Main.class.getResourceAsStream("/output.jlr1");
            if (inputStreamJLR1 == null) {
                inputStreamJLR1 = Main.class.getResourceAsStream("./output.jlr1");
            }

            System.out.println("Parsing " + fileName + ".");

            // Parsing step.
            final ASTHead AST = new ASTHead(JavaParser.parseTokenList(tokens, new Scanner(inputStreamJLR1)));

            System.out.println("Weeding " + fileName + ".");

            LiteralWeeder.weed(AST);
            AbstractMethodWeeder.weed(AST);
            ClassModifierWeeder.weed(AST);
            MethodModifierWeeder.weed(AST);
            FieldModifierWeeder.weed(AST);
            ClassNameWeeder.weed(AST, fileName);

            TypeLinker.assignTypesToNames(AST);
            AST.printAST();

            // Associates the AST with the class name.
            classTable.add(new ClassScope(new File(fileName).getName().split("\\.")[0], AST));
        }

        // Generate a set of all packages in the program.
        final HashSet<String> packageSet = classTable.stream()
                .map(c -> c.packageName.getQualifiedName())
                .collect(Collectors.toCollection(HashSet::new));

        // Creates a map of package scopes.
        final HashMap<String, PackageScope> packageMap = new HashMap<>();
        for (final String packageName : packageSet) {
            final PackageScope packageScope = new PackageScope();
            for (final ClassScope javaClass : classTable) {
                if (packageName.equals(javaClass.packageName.getQualifiedName()))
                    packageScope.addClass(javaClass);
            }
            packageMap.put(packageName, packageScope);
        }

        classTable.forEach(c -> c.packageMap.putAll(packageMap));

        // Link all types to their fully qualified name.
        for (final ClassScope classScope : classTable) {
            classScope.generateImportMaps(classTable);
//            classScope.linkTypesToQualifiedNames(null);
//            classScope.checkTypeSoundness();
        }

        // Checks for duplicate classes.
        for (int i = 0; i < classTable.size(); ++i) {
            for (int j = i + 1; j < classTable.size(); ++j) {
                if (classTable.get(i).equals(classTable.get(j))) {
                    System.err.println("Found duplicate class in same package.");
                    System.exit(42);
                }
            }
            classTable.get(i).duplicateCheck();
        }

        // Find the Object class.
        ClassScope objectScope = null;
        for (final ClassScope scope : classTable) {
            if (scope.isJavaLangObject()) {
                objectScope = scope;
                break;
            }
        }

        if (objectScope == null) {
            System.err.println("Could not identify java.lang.Object. Aborting!");
            System.exit(42);
        }

        // Generates the methods that every interface specially has from the Object class.
        for (final ClassScope classScope : classTable) {
            if (classScope.classType == INTERFACE) {
                classScope.generateObjectMethods(objectScope.methodTable);
            }
        }

        // Generates a map of all the classes in the program.
        final HashMap<String, ClassScope> classMap = new HashMap<>();
        for (final ClassScope classScope : classTable) {
            classMap.put(classScope.packageName
                            .generateAppendedPackageName(classScope.name)
                            .getDefaultlessQualifiedName(),
                    classScope);
        }

        for (final ClassScope classScope : classTable) {
            classScope.linkSuperTypes();
            classScope.linkImplementsTypes();
            classScope.linkMethodTypes();
        }

        for (final ClassScope classScope : classTable) {
            classScope.linkFieldsTypes();
        }

        for (final ClassScope classScope : classTable) {
            classScope.setClassMap(classMap);
        }

        for (final ClassScope classScope : classTable) {
            classScope.linkTypesToQualifiedNames(null);
        }

        for (final ClassScope classScope : classTable) {
            classScope.checkTypeSoundness();
        }

        TypeLinker.link(classTable, packageMap);

        // Checks that the class hierarchy is correct.
        final HierarchyChecker hCheck = new HierarchyChecker(classTable, classMap);
        hCheck.checkSuperCycles();
        hCheck.followsClassHierarchyRules();
        hCheck.followsMethodHierarchyRules();

        classTable.forEach(ClassScope::assignReachability);
        classTable.forEach(ClassScope::checkReachability);

        generateInterfaceSignatureSet();

        // Generates any static, non-class code.
        final ArrayList<String> staticExecCode = new ArrayList<>();
        staticExecCode.add("section .data");

        // Generates the SIT code.
        for (final ClassScope classScope : classTable) {
            staticExecCode.add(classScope.setSITLabel());
            for (final MethodScope methodScope : interfaceSignatureSet) {
                // TODO: Implement what the SIT does.
            }
        }

        for (final ClassScope classScope : classTable) {
            staticExecCode.add(classScope.setSubtypeTableLabel());
            // TODO: Implement the subtype table.
        }

        classTable.forEach(ClassScope::generatei386Code);

        staticExecCode.add("section .bss");
        // TODO: Define all static field for each class.

        staticExecCode.add("section .text");
        staticExecCode.add("global _start");
        staticExecCode.add("_start");

        // TODO: Resolve all static fields for each class.

        // TODO: Jump to the start of test method.

        // TODO: Print static exec code to a file.

        System.exit(0);
    }

    private static void generateInterfaceSignatureSet() {
        // Get all the interface methods.
        for (final ClassScope classScope : classTable) {
            if (classScope.classType == INTERFACE) {
                // Duplicate signatures should be weeded out by set.
                interfaceSignatureSet.addAll(classScope.methodTable);
            }
        }
    }
}

