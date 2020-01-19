package com.project;

class StateTrans {
    String input;
    State nextState;

    public StateTrans(String input, State nextState) {
        this.input = input;
        this.nextState = nextState;
    }
}
