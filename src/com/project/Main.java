package com.project;

import com.project.environments.ClassScope;
import com.project.environments.ast.ASTHead;
import com.project.heirarchy_checker.HierarchyChecker;
import com.project.parser.JavaParser;
import com.project.parser.structure.ParserSymbol;
import com.project.scanner.JavaScanner;
import com.project.type_linker.TypeLinker;
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
import java.util.Scanner;

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
System.out.println(inputStreamJLR1);
System.out.println(Main.class.getCanonicalName());
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

        // Checks for duplicate classes.
        for (int i = 0; i < classTable.size(); ++i) {
            System.out.println("Class: " + classTable.get(i).name);
            for (int j = i + 1; j < classTable.size(); ++j) {
                if (classTable.get(i).equals(classTable.get(j))) {
                    System.err.println("Found duplicate class in same package.");
                    System.exit(42);
                }
            }
        }

        HashMap<String, ClassScope> classMap = new HashMap<>();

        for (ClassScope javaClass: classTable) {
            String name = "";
            if (javaClass.packageName == null) name = javaClass.name;
            else name = javaClass.packageName.getQualifiedName() + "." + javaClass.name;
            classMap.put(name, javaClass);
        }

        TypeLinker.link(classTable, classMap);
        HierarchyChecker hCheck = new HierarchyChecker(classTable, classMap);


        if (!hCheck.followsClassHierarchyRules()) {
            System.exit(42);
        }

        if (hCheck.cycleDetected()) {
            System.exit(42);
        }

        if (!hCheck.followsMethodHierarchyRules()) {
            System.exit(42);
        }

        System.exit(0);
    }
}

