package io.github.xivqn.entities;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a graph.
 */
public class Graph implements Serializable {

    protected int numNodes;
    protected int numEdges;

    private final int[] sources;
    private final Set<Integer> sourcesSet;
    private final double[] probabilities;

    protected final List<List<Integer>> adjacentsIn;
    protected final List<List<Integer>> adjacentsOut;

    // K: Original node, V: Mapped node (0-index)
    protected final Map<Integer, Integer> mappedNodes;
    // K: Mapped node (0-index), V: Original node
    protected final List<Integer> mappedIndexes;

    protected final Set<Integer> readNodes;
    protected final HashMap<Integer, HashSet<Integer>> readEdgesIn;
    protected final HashMap<Integer, HashSet<Integer>> readEdgesOut;

    private transient Comparator<Integer> adjacentsOrderedComparator = Comparator.comparingInt(this::degreeOutOf)
            .reversed().thenComparingInt(x -> x);

    /**
     * Create a new graph with the given data.
     * 
     * @param numNodes
     * @param numEdges
     */
    public Graph(int numNodes, int numEdges, int[] sources) {
        this.numNodes = 0;
        this.numEdges = 0;
        this.adjacentsIn = new ArrayList<>(numNodes);
        this.adjacentsOut = new ArrayList<>(numNodes);
        this.mappedNodes = new HashMap<>(numNodes);
        this.mappedIndexes = new ArrayList<>(numNodes);
        this.readNodes = new HashSet<>(numNodes);
        this.readEdgesIn = new HashMap<>(numNodes);
        this.readEdgesOut = new HashMap<>(numNodes);
        this.sources = sources;
        this.sourcesSet = Arrays.stream(sources).boxed().collect(Collectors.toSet());
        this.probabilities = new double[numNodes];

        for (int i = 0; i < numNodes; i++) {
            this.adjacentsIn.add(new ArrayList<>());
            this.adjacentsOut.add(new ArrayList<>());
            this.readEdgesIn.put(i, new HashSet<>());
            this.readEdgesOut.put(i, new HashSet<>());
        }
    }

    public Graph() {
        this(0, 0, new int[0]);
    }

    public void setProbabilities(double[] probabilities) {
        System.arraycopy(probabilities, 0, this.probabilities, 0, this.probabilities.length);
    }

    public int getNumNodes() {
        return numNodes;
    }

    public int getNumEdges() {
        return numEdges;
    }

    public List<Integer> getAdjacentsIn(int u) {
        return this.adjacentsIn.get(u);
    }

    public List<Integer> getAdjacentsOut(int u) {
        return this.adjacentsOut.get(u);
    }

    public void sortAdjacents() {
        for (int i = 0; i < this.getNumNodes(); i++) {
            this.sortAdjacents(i);
        }
    }

    public void sortAdjacents(int u) {
        this.adjacentsIn.get(u).sort(adjacentsOrderedComparator);
        this.adjacentsOut.get(u).sort(adjacentsOrderedComparator);
    }

    public Comparator<Integer> getAdjacentsComparator() {
        return adjacentsOrderedComparator;
    }

    /**
     * Returns the incoming degree of the given node
     * @param u The node to get the degree of
     * @return The incoming degree of the given node
     */
    public int degreeInOf(int u) {
        return this.adjacentsIn.get(u).size();
    }

    /**
     * Returns the outgoing degree of the given node
     * @param u The node to get the degree of
     * @return The outgoing degree of the given node
     */
    public int degreeOutOf(int u) {
        return this.adjacentsOut.get(u).size();
    }

    /**
     * Adds an edge to the graph
     * @param u The first node
     * @param v The second node
     */
    public void addDirectedEdge(int u, int v) {
        u = mapNode(u);
        v = mapNode(v);

        if (u == v || this.readEdgesOut.get(u).contains(v) || this.readEdgesIn.get(v).contains(u)) {
            return;
        }

        this.adjacentsOut.get(u).add(v);
        this.adjacentsIn.get(v).add(u);
        this.readEdgesOut.get(u).add(v);
        this.readEdgesIn.get(v).add(u);
        this.numEdges++;
    }

    /**
     * Maps the given node to a 0-indexed node
     * @param node The node to map
     * @return The mapped node
     */
    protected int mapNode(int node) {
        int mappedNode;
        if (this.readNodes.contains(node)) {
            mappedNode = this.mappedNodes.get(node);
        } else {
            this.mappedNodes.put(node, this.numNodes);
            this.mappedIndexes.add(node);
            this.readNodes.add(node);
            mappedNode = this.numNodes++;
        }

        return mappedNode;
    }

    public Integer getInternalNode(int originalNode) {
        return this.mappedNodes.get(originalNode);
    }

    /**
     * Returns the list of nodes after unmapping them
     * @param nodes The list of nodes to unmap
     * @return The list of unmapped nodes
     */
    public List<Integer> getUnmappedNodes(Collection<Integer> nodes) {
        return nodes.stream().map(this::unmapNode).collect(Collectors.toList());
    }

    /**
     * Returns the list of nodes after mapping them
     * @param nodes The list of nodes to map
     * @return The list of mapped nodes
     */
    public List<Integer> getMappedNodes(Collection<Integer> nodes) {
        return nodes.stream().map(this::mapNode).collect(Collectors.toList());
    }

    /**
     * Unmaps the given node
     * @param node The node to unmap
     * @return The unmapped node
     */
    public int unmapNode(int node) {
        return this.mappedIndexes.get(node);
    }

    /**
     * Returns the list of nodes
     * @return The list of nodes
     */
    public Collection<Integer> getNodes() {
        return this.mappedNodes.values();
    }

    public int[] getSources() {
        return sources;
    }

    public Set<Integer> getSourcesSet() {
        return sourcesSet;
    }

    public double[] getProbabilities() {
        return probabilities;
    }

    public double getProbability(int node) {
        return getProbabilities()[node];
    }

    public void mapSources() {
        sourcesSet.clear();
        for (int i = 0; i < sources.length; i++) {
            sources[i] = mapNode(sources[i]);
            sourcesSet.add(sources[i]);
        }
    }

    public double getInLTValue(int node) {
        return 1.0 / this.adjacentsIn.get(node).size();
    }

    public double getOutLTValue(int node) {
        return 1.0 / this.adjacentsOut.get(node).size();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        adjacentsOrderedComparator = Comparator
                .comparingInt(this::degreeOutOf).reversed()
                .thenComparingInt(x -> x);
    }

}
