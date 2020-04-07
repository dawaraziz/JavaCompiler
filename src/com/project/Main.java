package com.project;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.FieldScope;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.project.environments.scopes.ClassScope.CLASS_TYPE.INTERFACE;

public class Main {

    public static final ArrayList<ClassScope> classTable = new ArrayList<>();
    public static final LinkedHashSet<MethodScope> interfaceSignatureSet = new LinkedHashSet<>();
    public static final HashMap<String, PackageScope> packageMap = new HashMap<>();
    public static MethodScope testMethod = null;
    public static ClassScope objectClass = null;

    public static final LinkedHashSet<String> methodExternList = new LinkedHashSet<>();
    public static final LinkedHashSet<String> staticExternList = new LinkedHashSet<>();


    public static void main(final String[] args) {

        if (args.length < 1) {
            System.err.println("No argument passed; expected file name.");
            System.exit(42);
        }

        for (final String fileName : args) {
            System.out.println("Scanning " + fileName + ".");
            final ArrayList<ParserSymbol> tokens = JavaScanner.tokenizeFile(fileName);

            // Default to Linux standard.
            InputStream inputStreamJLR1 = Main.class.getResourceAsStream("/output.jlr1");

            // If Linux standard doesn't work, try Windows standard.
            if (inputStreamJLR1 == null) {
                inputStreamJLR1 = Main.class.getResourceAsStream("./output.jlr1");
            }

            // Can't seem to find the jlr1 file; abort.
            if (inputStreamJLR1 == null) {
                System.err.println("Could not identify jlr1 grammar; aborting!");
                System.exit(42);
            }

            System.out.println("Parsing " + fileName + ".");
            final ASTHead AST = new ASTHead(JavaParser.parseTokenList(tokens, new Scanner(inputStreamJLR1)));

            System.out.println("Weeding " + fileName + ".");
            LiteralWeeder.weed(AST);
            AbstractMethodWeeder.weed(AST);
            ClassModifierWeeder.weed(AST);
            MethodModifierWeeder.weed(AST);
            FieldModifierWeeder.weed(AST);
            ClassNameWeeder.weed(AST, fileName);

            System.out.println("Disambiguating names in " + fileName + ".");
            TypeLinker.assignTypesToNames(AST);

            // Associates the AST with the class name.
            classTable.add(new ClassScope(new File(fileName).getName().split("\\.")[0], AST));
        }

        // Generate a set of all packages in the program.
        final HashSet<String> packageSet = classTable.stream()
                .map(c -> c.packageName.getQualifiedName())
                .collect(Collectors.toCollection(HashSet::new));

        // Creates a map of package scopes.
        for (final String packageName : packageSet) {
            final PackageScope packageScope = new PackageScope();
            for (final ClassScope javaClass : classTable) {
                if (packageName.equals(javaClass.packageName.getQualifiedName()))
                    packageScope.addClass(javaClass);
            }
            packageMap.put(packageName, packageScope);
        }

        classTable.forEach(ClassScope::generateImportMaps);

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
        for (final ClassScope scope : classTable) {
            if (scope.isJavaLangObject()) {
                objectClass = scope;
                break;
            }
        }

        if (objectClass == null) {
            System.err.println("Could not identify java.lang.Object. Aborting!");
            System.exit(42);
        }

        // Generates the methods that every interface specially has from the Object class.
        for (final ClassScope classScope : classTable) {
            if (classScope.classType == INTERFACE) {
                classScope.generateObjectMethods();
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

        TypeLinker.link();

        // Checks that the class hierarchy is correct.
        final HierarchyChecker hCheck = new HierarchyChecker(classTable, classMap);
        hCheck.checkSuperCycles();
        hCheck.followsClassHierarchyRules();
        hCheck.followsMethodHierarchyRules();

        classTable.forEach(ClassScope::assignReachability);
        classTable.forEach(ClassScope::checkReachability);

        generateInterfaceSignatureSet();

        classTable.forEach(ClassScope::generateMethodOrder);

        generateExternList();

        generateStatici386Code();

        classTable.forEach(ClassScope::generatei386Code);

        System.exit(0);
    }

    private static void generateExternList() {
        for (final ClassScope classScope : classTable) {
            for (final MethodScope methodScope : classScope.codeMethodOrder) {
                methodExternList.add(methodScope.generateExternStatement());

                for (final ClassScope classScope1 : classTable) {
                    if (!classScope1.equals(classScope)) {
                        classScope1.methodExternList.add(methodScope.generateExternStatement());
                    }
                }
            }
        }
    }

    private static void generateStatici386Code() {
        final ArrayList<String> staticExecCode = new ArrayList<>();

        // Imports every method, for simplicity.
        staticExecCode.addAll(methodExternList);

        // Exports all the SIT labels.
        classTable.forEach(e -> staticExecCode.add(e.generateSITGlobalLabel()));
        classTable.forEach(e -> staticExternList.add(e.generateSITExternLabel()));

        // Exports all the subtype table labels.
        classTable.forEach(e -> staticExecCode.add(e.generateSubtypeGlobalLabel()));
        classTable.forEach(e -> staticExternList.add(e.generateSubtypeExternLabel()));

        // Create a variable for each static variable, see https://nasm.us/doc/nasmdoc6.html for common.
        final ArrayList<FieldScope> staticFields = new ArrayList<>();
        classTable.forEach(e -> staticFields.addAll(e.getStaticFields()));
        staticFields.forEach(e -> staticExecCode.add(e.generateStaticFieldCode() + " ; Static var."));
        staticFields.forEach(e -> staticExternList.add(e.generateStaticFieldExtern()));

        staticExecCode.add("");

        staticExecCode.add("section .data");

        // Generates the SIT code.
        for (final ClassScope classScope : classTable) {
            staticExecCode.add(classScope.setSITLabel());
            for (final MethodScope methodScope : interfaceSignatureSet) {
                if (classScope.codeMethodOrder.contains(methodScope)) {
                    MethodScope method = null;
                    for (final MethodScope orderedMethod : classScope.codeMethodOrder) {
                        if (orderedMethod.equals(methodScope)) {
                            method = orderedMethod;
                            break;
                        }
                    }

                    if (method == null) {
                        System.err.println("Method is contained, but not found. Aborting!");
                        System.exit(42);
                    }

                    staticExecCode.add("dd " + method.callLabel());
                } else {
                    staticExecCode.add("dd 0 ; Method " + methodScope.name);
                }
            }
        }

        // Generates the subtype table code.
        for (final ClassScope subClass : classTable) {
            staticExecCode.add(subClass.setSubtypeTableLabel());
            for (final ClassScope superClass : classTable) {
                if (subClass.isSubClassOf(superClass)) {
                    staticExecCode.add("dd 1 ; Subclass " + subClass.name + ", Superclass " + superClass.name);
                } else {
                    staticExecCode.add("dd 0 ; Subclass " + subClass.name + ", Superclass " + superClass.name);
                }
            }
        }

        staticExecCode.add("section .text");
        staticExecCode.add("global _start");
        staticExecCode.add("_start:");

        // Initializes all our static fields.
        for (final FieldScope fieldScope : staticFields) {
            if (fieldScope.initializer != null) {
                staticExecCode.addAll(fieldScope.generateStaticInitializationCode());
            }
        }

        // Calls the "static int test()" method.
        staticExecCode.add("call " + testMethod.callLabel());

        // Exits.
        staticExecCode.add("mov ebx, eax");
        staticExecCode.add("mov eax, 1");
        staticExecCode.add("int 0x80");

        writeCodeToFile("static_exec.s", staticExecCode);
    }

    private static void generateInterfaceSignatureSet() {
        for (final ClassScope classScope : classTable) {
            if (classScope.classType == INTERFACE) {
                interfaceSignatureSet.addAll(classScope.methodTable);
            }
        }
    }

    public static void writeCodeToFile(final String name, final ArrayList<String> text) {
        System.out.println("Start of " + name + ": =============================");
        text.forEach(System.out::println);
        System.out.println("End of code: =============================");

//        final File file = new File("./output/" + name);
//
//        try {
//            if (!file.createNewFile()) throw new IOException();
//
//            final FileWriter fileWriter = new FileWriter(file);
//            for (final String s : text) {
//                fileWriter.write(s);
//            }
//            fileWriter.close();
//        } catch (final IOException e) {
//            System.err.println("Could not write file; aborting!");
//            System.err.println(e.toString());
//            System.exit(42);
//        }
    }
}

