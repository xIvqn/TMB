package io.github.xivqn.solvers;

import io.github.xivqn.entities.Graph;
import io.github.xivqn.entities.Instance;
import io.github.xivqn.exceptions.ImportException;
import io.github.xivqn.exceptions.ModelException;

public class STMBLTSolver extends STMBSolver {

    public STMBLTSolver(Instance instance, double gamma) throws ModelException, ImportException {
        super(instance, gamma);
    }

    @Override
    public float getProbability(Graph graph, int u, int v) {
        return (float) graph.getOutLTValue(u);
    }

}
