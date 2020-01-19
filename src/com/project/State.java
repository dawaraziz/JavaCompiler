package com.project;

import java.util.ArrayList;
import java.util.List;

class State {

    static State ERRSTATE = new State("ERR", false, null);

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
}
