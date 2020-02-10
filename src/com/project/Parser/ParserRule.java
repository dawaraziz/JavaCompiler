package com.project.Parser;

import java.util.ArrayList;
import java.util.Collections;

public class ParserRule {
    public String input;
    public ArrayList<String> output;

    public ParserRule(String rule) {
        String[] splitRule = rule.split(" ");
        this.input = splitRule[0];
        output = new ArrayList<>();
        for (int i = 1; i < splitRule.length; ++i) {
            output.add(splitRule[i]);
        }
        Collections.reverse(output);
    }
}
