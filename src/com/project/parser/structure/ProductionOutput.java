package com.project.parser.structure;

public class ProductionOutput {

    public enum ActionType {
        SHIFT,
        REDUCE
    }

    public ActionType actionType;
    public final Integer rule;

    public ProductionOutput(final String actionType, final Integer rule) {
        if (actionType.equalsIgnoreCase("SHIFT")) {
            this.actionType = ActionType.SHIFT;
        } else if (actionType.equalsIgnoreCase("REDUCE")) {
            this.actionType = ActionType.REDUCE;
        } else {
            System.err.println("Action was not SHIFT or REDUCE; failing early.");
            System.exit(42);
        }

        this.rule = rule;
    }
}
