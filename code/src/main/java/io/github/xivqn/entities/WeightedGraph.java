package io.github.xivqn.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import io.github.xivqn.exceptions.ModelException;
import org.apache.commons.lang3.tuple.Pair;

public class WeightedGraph extends Graph implements Serializable {

    private final Map<Pair<Integer, Integer>, Float> weights;
    protected final boolean[] removedNodes;

    public WeightedGraph(int numNodes, int numEdges, int[] sources) {
        super(numNodes, numEdges, sources);
        this.weights = new HashMap<>();
        this.removedNodes = new boolean[numNodes];
    }

    // No-argument constructor for deserialization
    public WeightedGraph() {
        super(0, 0, new int[0]);
        this.weights = new HashMap<>();
        this.removedNodes = new boolean[0];
    }

    public void addDirectedEdge(int source, int destination, float weight) {
        super.addDirectedEdge(source, destination);
        weights.put(Pair.of(mapNode(source), mapNode(destination)), weight);
    }

    public void setWeightToEdge(int source, int destination, float weight) {
        weights.put(Pair.of(source, destination), weight);
    }

    public int getMappedNode(int originalNode) {
        if (this.readNodes.contains(originalNode)) {
            return this.mappedNodes.get(originalNode);
        } else {
            throw new ModelException("Node " + originalNode + " does not exist in the graph.");
        }
    }

    public float getWeight(int source, int destination) {
        return weights.getOrDefault(Pair.of(source, destination), 0.0f);
    }

    public void copyNodeMapping(WeightedGraph other) {
        for (int i = 0; i < other.mappedIndexes.size(); i++) {
            this.mapNode(other.unmapNode(i));
        }
    }

    public static void storeToPath(WeightedGraph graph, String path) {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(path))) {
            oos.writeObject(graph);
        } catch (Exception e) {
            throw new ModelException("Failed to store WeightedGraph to path: " + path, e);
        }
    }
    public static WeightedGraph importFromPath(String path) {
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(path))) {
            return (WeightedGraph) ois.readObject();
        } catch (Exception e) {
            throw new ModelException("Failed to import WeightedGraph from path: " + path, e);
        }
    }

}
