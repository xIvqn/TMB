package io.github.xivqn.entities;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TMBSolution {

    private final boolean[] deactivate;
    private int deactivationCount;

    private double spread;

    private final Graph graph;

    public TMBSolution(Graph graph) {
        this(graph, false);
    }

    public TMBSolution(Graph graph, boolean defaultValue) {
        this.deactivate = new boolean[graph.getNumNodes()];
        if (defaultValue) Arrays.fill(this.deactivate, true);
        this.deactivationCount = defaultValue ? graph.getNumNodes() : 0;
        this.graph = graph;
    }

    public int getDeactivationCount() {
        return deactivationCount;
    }

    public void activate(int node) {
        if (!deactivate[node]) return;

        this.deactivate[node] = false;
        this.deactivationCount--;
    }

    public void deactivate(int node) {
        if (deactivate[node]) return;

        this.deactivate[node] = true;
        this.deactivationCount++;
    }

    public boolean isDeactivated(int node) {
        return deactivate[node];
    }

    public int[] getSources() {
        return graph.getSources();
    }

    public double getProbability(int node) {
        return graph.getProbabilities()[node];
    }

    public List<Integer> buildDeactivatedNodes() {
        return IntStream.range(0, deactivate.length)
                .filter(i -> deactivate[i])
                .boxed()
                .collect(Collectors.toList());
    }

    public void clearDeactivatedNodes() {
        Arrays.fill(deactivate, false);
        deactivationCount = 0;
    }

    public void setSpread(double spread) {
        this.spread = spread;
    }

    public double getSpread() {
        return spread;
    }

    public double getLTValue(int node) {
        return graph.getInLTValue(node);
    }

}
