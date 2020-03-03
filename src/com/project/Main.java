package com.project;

import com.project.environments.ClassScope;
import com.project.environments.ast.ASTHead;
import com.project.environments.structure.Name;
import com.project.hierarchy.HierarchyChecker;
import com.project.parser.JavaParser;
import com.project.parser.structure.ParserSymbol;
import com.project.scanner.JavaScanner;
import com.project.type_linker.PackageScope;
import com.project.type_linker.TypeLinker;
import com.project.weeders.AbstractMethodWeeder;
import com.project.weeders.ClassModifierWeeder;
import com.project.weeders.ClassNameWeeder;
import com.project.weeders.FieldModifierWeeder;
import com.project.weeders.LiteralWeeder;
import com.project.weeders.MethodModifierWeeder;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class Main {
    public static void main(final String[] args) {

        if (args.length < 1) {
            System.err.println("No argument passed; expected file name.");
            System.exit(42);
        }

        final ArrayList<ClassScope> classTable = new ArrayList<>();
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

            TypeLinker.disambiguate(AST);
            AST.printAST();

            // Associates the AST with the class name.
            classTable.add(new ClassScope(new File(fileName).getName().split("\\.")[0], AST));
        }

        // Link all types to their fully qualified name.
        for (final ClassScope classScope : classTable) {
            classScope.generateImportMaps(classTable);
            classScope.linkTypes();
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
            if (scope.name.equals("Object")
                    && scope.packageName.equals(Name.generateJavaLangPackageName())) {
                objectScope = scope;
                break;
            }
        }

        if (objectScope == null) {
            System.err.println("Could not identify java.lang.Object. Aborting!");
            System.exit(42);
        }

        for (final ClassScope classScope : classTable) {
            if (classScope.type == ClassScope.CLASS_TYPE.INTERFACE) {
                classScope.generateObjectMethods(objectScope.methodTable);
            }
        }

        HashMap<String, ClassScope> classMap = new HashMap<>();

        for (ClassScope javaClass: classTable) {
            String name = "";
            if (javaClass.packageName == null) name = javaClass.name;
            else {
                String packageN = javaClass.packageName.getQualifiedName();
                String split[];
                if (packageN.contains(".")) {
                    split = packageN.split("\\.");
                }
                else split = new String[]{packageN};
                System.out.println(packageN);
                System.out.println(split.length);
                if (split[0].contains("default#")) {
                    String [] newSplit = Arrays.copyOfRange(split, 1, split.length);
                    if (newSplit.length > 1) name = String.join(".", newSplit) + "." + javaClass.name;
                    else name = javaClass.name;
                    System.out.println(name);
                } else {
                    name = javaClass.packageName.getQualifiedName() + "." + javaClass.name;
                }
            }
            classMap.put(name, javaClass);
        }


        // Get all declared packages
        HashSet<String> package_names = new HashSet<>();
        for (ClassScope javaClass: classTable) {
            // given the AST we need to get the package declarations qualified name if it exists
            package_names.add(javaClass.packageName.getQualifiedName());
        }

        // Create each package its own scope
        HashMap<String, PackageScope> packages = new HashMap();
        for (String pkg_name : package_names){
            PackageScope pkg = new PackageScope(pkg_name);
            for (ClassScope javaClass: classTable) {
                if(pkg_name.equals(javaClass.packageName.getQualifiedName()))
                    pkg.addClassToScope(javaClass);
            }
            packages.put(pkg_name, pkg);
        }

        TypeLinker.link(classTable, classMap, packages);

        HierarchyChecker hCheck = new HierarchyChecker(classTable, classMap);


        if (!hCheck.followsClassHierarchyRules()) {
            System.out.println("1");
            System.exit(42);
        }

        if (hCheck.cycleDetected()) {
            System.out.println("2");
            System.exit(42);
        }

        if (!hCheck.followsMethodHierarchyRules()) {
            System.out.println("3");
            System.exit(42);
        }

        System.exit(0);
    }
}

