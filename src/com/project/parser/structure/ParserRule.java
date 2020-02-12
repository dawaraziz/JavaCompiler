package com.project.parser.structure;

import java.util.ArrayList;
import java.util.Collections;

public class ParserRule {
    public String input;
    public ArrayList<String> output;

    public ParserRule(final String rule) {
        final String[] splitRule = rule.split(" ");
        this.input = splitRule[0];
        output = new ArrayList<>();
        for (int i = 1; i < splitRule.length; ++i) {
            output.add(splitRule[i].toUpperCase());
        }
        Collections.reverse(output);
    }
}
