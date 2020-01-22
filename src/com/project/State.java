package com.project;

import java.util.ArrayList;
import java.util.List;

import static com.project.ScannerDFA.ERRSTATE;

class State {

    String name;
    boolean accept;
    Kind kind;
    List<StateTrans> transitions = new ArrayList<>();

    State(String name, boolean accept, Kind kind) {
        this.name = name;
        this.accept = accept;
        this.kind = kind;
    }

    void addTransition(String input, State nextState) {
        transitions.add(new StateTrans(input, nextState));
    }

    State nextState(char input) {
        State ret = ERRSTATE;

        for (StateTrans stateTrans : transitions) {
            if (stateTrans.input == null) return stateTrans.nextState;
            for (char c : stateTrans.input.toCharArray()) {
                if (input == c) return stateTrans.nextState;
            }
        }

        return ret;
    }
}
