package com.project.Parser;

public class ParserState {

    enum symbolType {
        Terminal,
        nonTerminal
    };

    enum actionType {
        Reduce,
        Shift
    };

    static class stateTransition {
        ParseTree input;
        ParserState newState;
        ProductionRule productionRule;

        public stateTransition(ParseTree input) {

            this.input = input;
        }
    }
}
