package com.project.parser.structure;

public final class ProductionInput {
    private final Integer state;
    private final String lookahead;

    public ProductionInput(final Integer state, final String lookahead) {
        this.state = state;
        this.lookahead = lookahead.toUpperCase();
    }

    public String toString() {
        return "State: " + state + " , Lookahead: " + lookahead + ".";
    }

    public int hashCode() {
        return state.hashCode() + lookahead.hashCode();
    }

    public boolean equals(final Object other) {
        if (other instanceof ProductionInput) {
            final ProductionInput p = (ProductionInput) other;
            return state.equals(p.state) && lookahead.equals(p.lookahead);
        }
        return false;
    }
}
