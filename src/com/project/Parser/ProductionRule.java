package com.project.Parser;

public class ProductionRule {
    int startState;
    String lookahead;
    String action;
    int endState;

    public ProductionRule(int startState, String lookahead, String action, int endState) {
        this.startState = startState;
        this.lookahead = lookahead;
        this.action = action;
        this.endState = endState;
    }
}
