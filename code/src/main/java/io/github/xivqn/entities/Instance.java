package io.github.xivqn.entities;

/**
 * Represents an instance social network problem.
 */
public class Instance {

    private Graph graph;
    private String name;

    public Instance(Graph graph, String name) {
        this.graph = graph;
        this.name = name;
    }

    public Graph getGraph() {
        return graph;
    }

    public String getName() {
        return name;
    }

}
