package com.project;

import com.project.Parser.ParseTree;
import com.project.Parser.ParserRule;
import com.project.Parser.ParserState;
import com.project.Weeders.LiteralWeeder;
import com.project.scanner.JavaScanner;
import javafx.util.Pair;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class Main {
    public static void main(String[] args) {

        if (args.length < 1) System.exit(42);

        ArrayList<ParseTree> tokens = JavaScanner.tokenizeFile(args[0]);
        LiteralWeeder.weed(tokens);

        for (ParseTree token : tokens) {
            System.out.println(token.getLexeme() + " " + token.getKind());
        }

        Class fileClass = Main.class;
        InputStream inputStreamCFG = fileClass.getResourceAsStream("input.cfg");

        Scanner scanner = new Scanner(inputStreamCFG);
        int passInt = scanner.nextInt();
        for (int i = 0; i <= passInt; ++i) {
            scanner.nextLine();
        }
        passInt = scanner.nextInt();
        for (int i = 0; i <= passInt; ++i) {
            scanner.nextLine();
        }
        String startSymbol = scanner.nextLine();
        passInt = scanner.nextInt();
        scanner.nextLine();
        ArrayList<ParserRule> reductionRules = new ArrayList<>();
        for (int i = 0; i < passInt; ++i) {
            String rule = scanner.nextLine();
            reductionRules.add(new ParserRule(rule.toUpperCase()));
        }
        InputStream inputStreamJLR1 = fileClass.getResourceAsStream("output.jlr1");

        ArrayList<ParserState> states = new ArrayList<>();
        scanner = new Scanner(inputStreamJLR1);
        int stateNumber = scanner.nextInt();
        for (int i = 0; i < stateNumber; ++i) {
            states.add(new ParserState());
        }

        HashMap<Pair<Integer, String>, Pair<String, Integer>> productionRules = new HashMap<>();
        int transitionNumber = scanner.nextInt();
        for (int i = 0; i < transitionNumber; ++i) {
            int startState = scanner.nextInt();
            String lookahead = scanner.next().toUpperCase();
            String action = scanner.next();
            int newState = scanner.nextInt();

            productionRules.put(new Pair<>(startState, lookahead), new Pair<>(action, newState));
        }

        Stack<ParseTree> parseStack = new Stack<>();
        Stack<Integer> statesVisited = new Stack<>();

        Stack<ParseTree> inputStack = new Stack<>();

        Collections.reverse(tokens);
        for (ParseTree token : tokens) {
            inputStack.push(token);
        }

        statesVisited.push(0);

        while (true) {

            ParseTree token = inputStack.peek();

            if (!parseStack.isEmpty() && parseStack.peek().getLexeme().equals("COMPILATIONUNIT") && token.getLexeme().equals("EOF")) {
                break;
            }

            String lookahead = null;
            if (token.getKind() == null) {
                lookahead = token.getLexeme();
            } else {
                lookahead = token.getKind().toString();
            }
            Pair currentLRState = new Pair<>(statesVisited.peek(), lookahead);
            Pair<String, Integer> action = productionRules.get(currentLRState);

            if (action == null) {
                System.err.println("Could not find production rule for pair: " + currentLRState.toString());
                System.exit(42);
            }

            if (action.getKey().equals("shift")) {
                parseStack.push(inputStack.pop());
                statesVisited.push(action.getValue());
            } else {
                ParserRule rule = reductionRules.get(action.getValue());

                ParseTree reducedSymbol = new ParseTree(null, rule.input, false, false, null);

                for (int i = 0; i < rule.output.size(); ++i) {
                    statesVisited.pop();
                    ParseTree stackTop = parseStack.pop();

                    String stackTopValue = null;
                    if (stackTop.getKind() == null) {
                        stackTopValue = stackTop.getLexeme();
                    } else {
                        stackTopValue = stackTop.getKind().toString();
                    }

                    if (!stackTopValue.equals(rule.output.get(i))) {
                        System.err.println("Expected " + rule.output.get(i) + ", saw " + stackTopValue);
                        System.exit(42);
                    }

                    stackTop.parent = reducedSymbol;
                    reducedSymbol.addChild(stackTop);
                }

                inputStack.push(reducedSymbol);
            }
        }

        printTree(parseStack.peek());

        System.exit(0);
    }

    static void printTree(ParseTree pTree) {
        if (pTree.isTerminal() || pTree.noChildren()) {
            if (!pTree.getLexeme().equals("")) System.out.println(pTree.getLexeme());
            return;
        }

        for (ParseTree child : pTree.getChildren()) {
            printTree(child);
        }
    }
}

