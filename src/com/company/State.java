package com.company;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

class State {

    static State ERRSTATE = new State("ERR", false, null);

    String name;
    boolean accept;
    Kind kind;
    List<StateTrans> transitions = new ArrayList<>();

    public State(String name, boolean accept, Kind kind) {
        this.name = name;
        this.accept = accept;
        this.kind = kind;
    }
}
