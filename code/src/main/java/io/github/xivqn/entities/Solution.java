package io.github.xivqn.entities;

import java.util.List;

public class Solution {

    private final String name;

    private final double of;

    private final long executionTime;

    private final List<Integer> selection;

    private final int selectionSize;

    public Solution(final String name, final double of, final long executionTime, final List<Integer> selection) {
        this.name = name;
        this.of = of;
        this.executionTime = executionTime;
        this.selection = selection;
        this.selectionSize = selection.size();
    }

    public String getName() {
        return name;
    }

    public double getOf() {
        return of;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public List<Integer> getSelection() {
        return selection;
    }

    public int getSelectionSize() {
        return selectionSize;
    }

}
