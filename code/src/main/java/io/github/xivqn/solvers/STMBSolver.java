package io.github.xivqn.solvers;

import io.github.xivqn.diffmodels.DiffusionModel;
import io.github.xivqn.diffmodels.DiffusionModelFactory;
import io.github.xivqn.entities.*;
import io.github.xivqn.exceptions.ImportException;
import io.github.xivqn.exceptions.ModelException;
import io.github.xivqn.exceptions.SolverException;
import io.github.xivqn.utils.ArgsUtils;
import io.github.xivqn.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Stack;

public abstract class STMBSolver implements Solver {

    protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private Instance instance;
    private DiffusionModel diffusionModel;
    private double gamma;
    private int numSamples;

    private int superNode;                  // Node in mergedGraph representing all sources
    private WeightedGraph[] sampleTrees;    // with same mapping as mergedGraph
    private String[] sampleTreesPaths;      // paths to each sampleTree file, used with --cache-trees
    private WeightedGraph mergedGraph;      // graph with superNode instead of sources, new mapping from
    private ArrayList<Integer>[][] paths;   // paths[i][j] is the path from root to node j in the sampleTrees[i]
    private int[] heuristic;                // heuristic[i] is the heuristic value of node i across all Trees

    public STMBSolver(Instance instance, double gamma) throws ModelException, ImportException {
        long startInitializationTime = System.currentTimeMillis();
        this.instance = instance;
        this.diffusionModel = DiffusionModelFactory.getModel(ArgsUtils.getDiffusionModelType(), instance);
        this.gamma = gamma;
        this.numSamples = ArgsUtils.getNumSamples();
        log.debug("STMB Solver for {} started in {} ms", instance.getName(), System.currentTimeMillis() - startInitializationTime);
    }

    @Override
    public Solution solve() throws SolverException {
        TMBSolution tmbSolution = new TMBSolution(instance.getGraph());
        long startTime = System.currentTimeMillis();
        diffusionModel.evaluate(tmbSolution);
        double currentSpread = tmbSolution.getSpread(), bestSpread = tmbSolution.getSpread();
        double targetSpread = currentSpread - currentSpread * gamma;
        int iterations = 0;
        log.info("Initial evaluation {}, target={} (gamma={})", currentSpread, targetSpread, gamma);

        precomputeTrees(instance.getGraph());

        while (currentSpread > targetSpread && System.currentTimeMillis() - startTime < ArgsUtils.getTimeLimit() * 1000L) {
            int node = getNode(heuristic);
            int heuristicValue = heuristic[node];
            iterations++;

            if (heuristicValue == 0) {
                throw new SolverException(String.format("No more nodes to deactivate (heuristicValue==0), stopping at iteration %d, current spread=%4.3f, best spread=%4.3f, target=%4.3f", iterations, currentSpread, bestSpread, targetSpread));
            }

            // Deactivate node in solution to block influence spread
            heuristic[node] = 0; // So it won't be chosen again
            tmbSolution.deactivate(mergedGraph.unmapNode(node));

            // Update heuristic values
            for (int i = 0; i < this.numSamples; i++) {
                WeightedGraph tree = getSampleTree(i);
                try {
                    tree.getMappedNode(node);
                } catch (ModelException e) {
                    continue; // Node not in this tree
                }
                updateDescendants(tree, tree.getMappedNode(node));
                updateAncestors(i, node);
            }

            // Re-evaluate spread
            diffusionModel.evaluate(tmbSolution);
            currentSpread = tmbSolution.getSpread();
            bestSpread = Math.min(bestSpread, currentSpread);

            log.debug("Iteration {}: deactivated node {} with heuristic={}, current spread={}, best spread={}, target={}",
                    iterations, mergedGraph.unmapNode(node), heuristicValue, currentSpread, bestSpread, targetSpread);
        }

        long endTime = System.currentTimeMillis();

        wipeSampleTreesFiles();

        return new Solution(
                instance.getName(),
                tmbSolution.getSpread(),
                endTime - startTime,
                instance.getGraph().getUnmappedNodes(tmbSolution.buildDeactivatedNodes())
        );
    }

    private void precomputeTrees(Graph graph) {
        long startTime = System.currentTimeMillis();

        MergeResult mergeResult = merge(graph, graph.getSources());
        mergedGraph = mergeResult.graph;
        this.superNode = mergeResult.superNode;
        log.trace("Merged graph has {} nodes and {} edges (supernode {})",
                mergedGraph.getNumNodes(), mergedGraph.getNumEdges(), superNode);

        initSampleTreesStorage();
        heuristic = new int[mergedGraph.getNumNodes()];
        paths = new ArrayList[this.numSamples][mergedGraph.getNumNodes()];
        for (int i = 0; i < this.numSamples; i++) {
            for (int j = 0; j < mergedGraph.getNumNodes(); j++) {
                paths[i][j] = new ArrayList<>();
            }
            buildTree(mergedGraph, superNode, i);
        }

        log.info("Precomputed {} sample trees in {} ms", this.numSamples, System.currentTimeMillis() - startTime);
    }

    private void buildTree(WeightedGraph mergedGraph, int root, int i) {
        WeightedGraph newGraph = new WeightedGraph(
                mergedGraph.getNumNodes(),
                0,
                new int[]{root}
        );
        newGraph.copyNodeMapping(mergedGraph);
        newGraph.mapSources();
        // Add edges with probability
        for (int j = 0; j < mergedGraph.getNumNodes(); j++) {
            for (int k = 0; k < mergedGraph.getAdjacentsOut(j).size(); k++) {
                // For each edge
                float weight = mergedGraph.getWeight(j, mergedGraph.getAdjacentsOut(j).get(k));
                if (RandomUtils.getRandom().nextFloat() <= weight) {
                    newGraph.addDirectedEdge(mergedGraph.unmapNode(j), mergedGraph.unmapNode(mergedGraph.getAdjacentsOut(j).get(k)), weight);
                }
            }
        }
        // Generate tree from newGraph
        saveSampleTree(i, dfsTree(newGraph, i, root));
        log.trace("Sample tree {} has {} nodes and {} edges", i, newGraph.getNumNodes(), newGraph.getNumEdges());
    }

    private WeightedGraph dfsTree(WeightedGraph graph, int treeIdx, int root) {
        boolean[] visited = new boolean[graph.getNumNodes()];
        WeightedGraph tree = new WeightedGraph(graph.getNumNodes(), 0, new int[]{root});
        tree.copyNodeMapping(graph);
        tree.mapSources();
        Stack<Integer> stack = new Stack<>();
        Stack<ArrayList<Integer>> pathStack = new Stack<>();
        stack.push(root);
        pathStack.push(new ArrayList<>());

        while (!stack.isEmpty()) {
            int v = stack.pop();
            ArrayList<Integer> currentPath = pathStack.pop();
            if (visited[v]) continue;

            // Visit node v
            visited[v] = true;
            ArrayList<Integer> pathToV = new ArrayList<>(currentPath);
            pathToV.add(v);
            paths[treeIdx][v] = pathToV;

            // Update heuristic values
            for (int nodeInPath : pathToV) {
                heuristic[nodeInPath]++;
            }

            // Explore neighbors
            for (int i : graph.getAdjacentsOut(v)) {
                if (!visited[i]) {
                    tree.addDirectedEdge(graph.unmapNode(v), graph.unmapNode(i), graph.getWeight(v, i));
                    stack.push(i);
                    ArrayList<Integer> pathToI = new ArrayList<>(pathToV);
                    pathStack.push(pathToI);
                }
            }
        }
        return tree;
    }

    private void updateAncestors(int i, int node) {
        // Update -1 in ancestors using paths
        ArrayList<Integer> path = paths[i][node];
        if (path != null) {
            for (int ancestor : path) {
                if (ancestor != node) {
                    heuristic[ancestor] = Math.max(0, heuristic[ancestor] - 1);
                }
            }
        }
    }

    private void updateDescendants(WeightedGraph tree, int node) {
        boolean[] visited = new boolean[tree.getNumNodes()];
        Stack<Integer> stack = new Stack<>();
        stack.push(node);
        while (!stack.isEmpty()) {
            int v = stack.pop();
            if (visited[v]) continue;
            visited[v] = true;
            heuristic[v] = 0; // Set 0 for all descendants (including node itself)
            for (int child : tree.getAdjacentsOut(v)) {
                stack.push(child);
            }
        }
    }

    private int getNode(int[] heuristic) {
        int bestNode = -1, bestHeuristic = -1;

        for (int node = 0; node < heuristic.length; node++) {
            if (node == superNode) continue; // Skip superNode
            if (heuristic[node] > bestHeuristic) {
                bestHeuristic = heuristic[node];
                bestNode = node;
            }
        }

        return bestNode;
    }

    public static class MergeResult {
        public WeightedGraph graph;
        public int superNode;
        public int infectedEdges;

        public MergeResult(WeightedGraph graph, int superNode, int infectedEdges) {
            this.graph = graph;
            this.superNode = superNode;
            this.infectedEdges = infectedEdges;
        }
    }

    public MergeResult merge(Graph graph, int[] sources) {
        superNode = graph.getNumNodes(); // New node index
        int[] newSources = new int[1];
        newSources[0] = superNode;
        WeightedGraph newGraph = new WeightedGraph(graph.getNumNodes() - sources.length + 1,
                0,
                newSources
        );
        newGraph.mapSources();
        int mappedSuperNode = newGraph.getSources()[0];

        // Copy graph without sources
        for (int i = 0; i < graph.getNumNodes(); i++) {
            if (!graph.getSourcesSet().contains(i)) { // If not a source
                for (int j : graph.getAdjacentsOut(i)) {
                    if (!graph.getSourcesSet().contains(j)) { // If not a source
                        newGraph.addDirectedEdge(i, j, getProbability(graph, i, j));
                    }
                }
            }
        }

        int infectedEdges = 0;
        // Add superNode edges
        for (int i : sources) {
            infectedEdges += graph.getAdjacentsOut(i).size();
            for (int j : graph.getAdjacentsOut(i)) {
                if (!graph.getSourcesSet().contains(j)) {
                    try {
                        int mappedJ = newGraph.getMappedNode(j);
                        if (!newGraph.getAdjacentsOut(mappedSuperNode).contains(mappedJ)) {
                            float weight = getProbability(graph, i, j);
                            newGraph.addDirectedEdge(superNode, j, weight);
                        } else {
                            float weight = newGraph.getWeight(mappedSuperNode, mappedJ) + (1 - newGraph.getWeight(mappedSuperNode, mappedJ)) * getProbability(graph, i, j);
                            newGraph.setWeightToEdge(mappedSuperNode, mappedJ, weight);
                        }
                    } catch (ModelException e) {
                        float weight = getProbability(graph, i, j);
                        newGraph.addDirectedEdge(superNode, j, weight);
                    }
                }
            }
        }

        superNode = mappedSuperNode;

        return new MergeResult(newGraph, superNode, infectedEdges);
    }

    public abstract float getProbability(Graph graph, int u, int v);

    private boolean compareMapping(Graph g1, Graph g2) {
        for (int i = 0; i < g1.getNumNodes(); i++) {
            try {
                int mapped1 = g1.unmapNode(i);
                int mapped2 = g2.unmapNode(i);
                if (mapped1 != mapped2) {
                    throw new RuntimeException("Mapping mismatch: node " + i + " in g1 maps from " + mapped1 + ", which maps from " + mapped2 + " in g2");
                }
            } catch (ModelException e) {
                throw new RuntimeException("Node " + i + " in g1 not found in g2");
            }
        }
        return true;
    }

    public void initSampleTreesStorage() {
        if (ArgsUtils.isCacheTrees()) {
            java.io.File cacheDir = new java.io.File(ArgsUtils.getTreeStoreParentDir());
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            sampleTreesPaths = new String[this.numSamples];
        } else {
            sampleTrees = new WeightedGraph[this.numSamples];
        }
    }

    public WeightedGraph getSampleTree(int i) {
        if (ArgsUtils.isCacheTrees()) {
            return WeightedGraph.importFromPath(sampleTreesPaths[i]);
        } else {
            return sampleTrees[i];
        }
    }

    public void saveSampleTree(int i, WeightedGraph tree) {
        if (ArgsUtils.isCacheTrees()) {
            String path = ArgsUtils.getTreeStoreParentDir() + "tree_" + instance.getName() + "_" + i + ".wgraph";

            WeightedGraph.storeToPath(tree, path);
            sampleTreesPaths[i] = path;
        } else {
            sampleTrees[i] = tree;
        }
    }

    public void wipeSampleTreesFiles() {
        if (ArgsUtils.isCacheTrees() && sampleTreesPaths != null) {
            for (String path : sampleTreesPaths) {
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    if (file.delete()) {
                        log.trace("Deleted sample tree file {}", path);
                    } else {
                        log.warn("Could not delete sample tree file {}", path);
                    }
                }
            }
        }
    }

}

