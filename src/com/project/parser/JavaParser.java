package com.project.parser;

import com.project.parser.structure.ParserSymbol;
import com.project.parser.structure.ParserRule;
import com.project.parser.structure.ProductionInput;
import com.project.parser.structure.ProductionOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import static com.project.parser.structure.ProductionOutput.ActionType.SHIFT;

public class JavaParser {
    public static ParserSymbol parseTokenList(final ArrayList<ParserSymbol> tokens,
                                              final Scanner LR1Scanner) {

        // Takes in the production rules used for reduce actions.
        final int reductionSize = Integer.parseInt(LR1Scanner.nextLine());
        final ArrayList<ParserRule> reductionRules = new ArrayList<>();
        for (int i = 0; i < reductionSize; ++i) {
            final String rule = LR1Scanner.nextLine();
            reductionRules.add(new ParserRule(rule));
        }

        // Takes in the production rules used for shift actions.
        final int productionSize = Integer.parseInt(LR1Scanner.nextLine());
        final HashMap<ProductionInput, ProductionOutput> productionRules = new HashMap<>();
        for (int i = 0; i < productionSize; ++i) {
            final ProductionInput input = new ProductionInput(LR1Scanner.nextInt(), LR1Scanner.next());
            final ProductionOutput output = new ProductionOutput(LR1Scanner.next(), LR1Scanner.nextInt());
            productionRules.put(input, output);
        }

        // We use three stacks in our parser. One to keep track of parsed input,
        // one to keep track of unparsed input, and one to keep track of our
        // current state.
        final Stack<ParserSymbol> parseStack = new Stack<>();
        final Stack<Integer> stateStack = new Stack<>();
        final Stack<ParserSymbol> inputStack = new Stack<>();

        // We need to reverse our tokens to put them in the stack. Otherwise,
        // the program will pop in reverse order!
        Collections.reverse(tokens);
        for (final ParserSymbol token : tokens) {
            inputStack.push(token);
        }

        // Push the start state onto our state stack.
        stateStack.push(0);

        // We exit once we see the compilation unit symbol and EOF simultaneously.
        while (parseStack.isEmpty()
                || parseStack.peek().equals("COMPILATIONUNIT")
                || inputStack.peek().equals("EOF")) {

            // Get the current lookahead symbol.
            final String lookahead = inputStack.peek().toString();

            // Query the parser for what action to take given our current state and lookahead.
            final ProductionInput currentState = new ProductionInput(stateStack.peek(), lookahead);
            final ProductionOutput action = productionRules.get(currentState);

            // If we can't find an action, gracefully exit.
            if (action == null) {
                System.err.println("Could not find production rule for " + currentState.toString());
                System.exit(42);
            }


            if (action.actionType == SHIFT) {

                // Shifts the lookahead symbol on the parsed stack.
                parseStack.push(inputStack.pop());
                stateStack.push(action.rule);

            } else {

                // Get the rule we need to reduce with.
                final ParserRule rule = reductionRules.get(action.rule);
                final ParserSymbol reducedSymbol = new ParserSymbol(null, rule.input);

                for (int i = 0; i < rule.output.size(); ++i) {

                    // To reduce, we pop the parsed symbols off the stack, while
                    // simultaneously backing up our state stack.
                    stateStack.pop();
                    final ParserSymbol stackTop = parseStack.pop();

                    // If the popped symbols and expected symbols don't match, gracefully exit.
                    if (!stackTop.toString().equals(rule.output.get(i))) {
                        System.err.println("Expected " + rule.output.get(i) + ", saw " + stackTop.toString());
                        System.exit(42);
                    }

                    // Add the popped symbol as a child of the new symbol, and vice versa.
                    stackTop.parent = reducedSymbol;
                    reducedSymbol.children.add(stackTop);
                }

                // Push the finished symbol onto the input stack to be immediately shifted.
                inputStack.push(reducedSymbol);
            }
        }

        // Returns the head of our tree.
        return parseStack.peek();
    }
}
