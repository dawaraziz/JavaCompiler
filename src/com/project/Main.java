package com.project;

import com.project.Parser.Jlalr1;
import com.project.Parser.ParsTree;
import com.project.Weeders.LiteralWeeder;
import com.project.scanner.JavaScanner;
import com.project.scanner.Token;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
//        ArrayList<Token> tokens = JavaScanner.tokenizeFile(args[0]);
//        LiteralWeeder.weed(tokens);
        // Parser


//        for (Token token : tokens) {
//            System.out.println(token.getLexeme() + " " + token.getKind());
//        }

        ParsTree pTree = new ParsTree("S", false, false, null);

        pTree.addChild("BOF", true, true, pTree);
        pTree.addChild("H", false, false, pTree);
        pTree.addChild("EOF", true, true, pTree);

        ParsTree root = pTree;

        pTree = pTree.getChild(1);
        pTree.addChild("PACKAGEopt", false, false, pTree);
        pTree.addChild("MULT_IMPORTopt", false, false, pTree);
        pTree.addChild("TYPE_DECLARATIONopt", false, false, pTree);

        pTree = pTree.getChild(0);
        pTree.addChild("package", true, true, pTree);
        pTree.addChild("TYPE_NAME", false, false, pTree);
        pTree.addChild(";", true, true, pTree);

        pTree = pTree.getChild(1);
        pTree.addChild("PACKAGE_OR_TYPE", false, false, pTree);
        pTree.addChild(".", true, true, pTree);
        pTree.addChild("VARID", false, false, pTree);

        pTree = pTree.getChild(0);
        pTree.addChild("VARID", false, false, pTree);

        pTree = pTree.getChild(0);
        pTree.addChild("com", true, true, pTree);
        pTree = pTree.getParent().getParent();

        pTree = pTree.getChild(2);
        pTree.addChild("project", true, true, pTree);
        pTree = pTree.getParent().getParent().getParent();

        pTree = pTree.getChild(1);
        pTree.addChild("EPSILON", true, true, pTree);
        pTree = pTree.getParent();

        pTree = pTree.getChild(2);
        pTree.addChild("CLASS_MODIFIER", false, false, pTree);
        pTree.addChild("class", true, true, pTree);
        pTree.addChild("VARID", false, false, pTree);
        pTree.addChild("SUPER", false, false, pTree);
        pTree.addChild("INTERFACE", false, false, pTree);
        pTree.addChild("{", true, true, pTree);
        pTree.addChild("CLASS_BODY", false, false, pTree);
        pTree.addChild("}", true, true, pTree);

        pTree = pTree.getChild(0);
        pTree.addChild("public", true, true, pTree);
        pTree = pTree.getParent();

        pTree = pTree.getChild(2);
        pTree.addChild("A", true, true, pTree);
        pTree = pTree.getParent();

        pTree = pTree.getChild(3);
        pTree.addChild("EPSILON", true, true, pTree);
        pTree = pTree.getParent();

        pTree = pTree.getChild(4);
        pTree.addChild("EPSILON", true, true, pTree);
        pTree = pTree.getParent();

        pTree = pTree.getChild(6);
        pTree.addChild("PROPERTY_DECLARATION", false, false, pTree);
        pTree.addChild("CLASS_BODY", false, false, pTree);

        pTree = pTree.getChild(1);
        pTree.addChild("EPSILON", true, true, pTree);
        pTree = pTree.getParent();

        pTree = pTree.getChild(0);
        pTree.addChild("PROPERTY_MODIFIERS", false, false, pTree);
        pTree.addChild("TYPE", false, false, pTree);
        pTree.addChild("VARID", false, false, pTree);
        pTree.addChild("INSTANTIATION", false, false, pTree);
        pTree.addChild(";", true, true, pTree);

        pTree = pTree.getChild(0);
        pTree.addChild("public", true, true, pTree);
        pTree = pTree.getParent();

        pTree = pTree.getChild(1);
        pTree.addChild("int", true, true, pTree);
        pTree = pTree.getParent();

        pTree = pTree.getChild(2);
        pTree.addChild("a", true, true, pTree);
        pTree = pTree.getParent();

        pTree = pTree.getChild(3);
        pTree.addChild("=", true, true, pTree);
        pTree.addChild("EXPRESSION", false, false, pTree);

        pTree = pTree.getChild(1);
        pTree.addChild("NUM", false, false, pTree);
        pTree.addChild("+", true, true, pTree);
        pTree.addChild("NUM", false, false, pTree);

        pTree = pTree.getChild(0);
        pTree.addChild("1", true, true, pTree);
        pTree = pTree.getParent();

        pTree = pTree.getChild(2);
        pTree.addChild("1", true, true, pTree);

        printTree(root);

        int x = 1-1;

        System.exit(0);
    }

    public static void printTree(ParsTree pTree) {
        if (pTree.isTerminal() || pTree.noChildren()) {
            if (pTree.getValue() != "EPSILON") System.out.println(pTree.getValue());
            return;
        }

        for (ParsTree child: pTree.getChildren()) {
            printTree(child);
        }

    }
}

