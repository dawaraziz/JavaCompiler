package com.project.scanner;

import java.util.ArrayList;
import java.util.List;

import static com.project.scanner.ScannerDFA.ERRSTATE;

class State {

    final boolean accept;
    final Kind kind;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String name;
    private final List<StateTrans> transitions = new ArrayList<>();

    static class StateTrans {
        final String input;
        final State nextState;

        StateTrans(final String input, final State nextState) {
            this.input = input;
            this.nextState = nextState;
        }
    }

    State(final String name, final boolean accept, final Kind kind) {
        this.name = name;
        this.accept = accept;
        this.kind = kind;
    }

    void addTransition(final String input, final State nextState) {
        transitions.add(new StateTrans(input, nextState));
    }

    State nextState(final char input) {
        for (StateTrans stateTrans : transitions) {
            if (stateTrans.input == null) return stateTrans.nextState;
            for (char c : stateTrans.input.toCharArray()) {
                if (input == c) return stateTrans.nextState;
            }
        }

        return ERRSTATE;
    }
}
