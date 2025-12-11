package io.github.xivqn.solvers;

import io.github.xivqn.diffmodels.DiffusionModel;
import io.github.xivqn.diffmodels.DiffusionModelFactory;
import io.github.xivqn.entities.Instance;
import io.github.xivqn.entities.Solution;
import io.github.xivqn.entities.TMBSolution;
import io.github.xivqn.exceptions.ImportException;
import io.github.xivqn.exceptions.ModelException;
import io.github.xivqn.exceptions.SolverException;
import io.github.xivqn.utils.ArgsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GreedySolver implements Solver {

    private static final Logger log = LoggerFactory.getLogger(GreedySolver.class);

    private Instance instance;
    private DiffusionModel diffusionModel;
    private double gamma;

    public GreedySolver(Instance instance, double gamma) throws ModelException, ImportException {
        long startInitializationTime = System.currentTimeMillis();
        this.instance = instance;
        this.diffusionModel = DiffusionModelFactory.getModel(ArgsUtils.getDiffusionModelType(), instance);
        this.gamma = gamma;
        log.debug("TMB Solver for {} started in {} ms", instance.getName(), System.currentTimeMillis() - startInitializationTime);
    }

    protected List<Integer> buildPool(Instance instance) {
        List<Integer> nodes = new ArrayList<>(instance.getGraph().getNumNodes());

        for (int i = 0; i < instance.getGraph().getNumNodes(); i++) nodes.add(i);

        return nodes;
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

        while (currentSpread > targetSpread) {
            iterations++;

            Best best = getBest(tmbSolution, currentSpread);
            if (best == null) break;

            tmbSolution.deactivate(best.bestNode);
            diffusionModel.evaluate(tmbSolution);
            currentSpread = tmbSolution.getSpread();
            bestSpread = Math.min(bestSpread, currentSpread);

            log.debug("Iteration {}: deactivated node {} with delta={}, current spread={}, best spread={}, target={}",
                    iterations, best.bestNode, best.maxDelta, currentSpread, bestSpread, targetSpread);
        }

        long endTime = System.currentTimeMillis();

        return new Solution(
                instance.getName(),
                tmbSolution.getSpread(),
                endTime - startTime,
                instance.getGraph().getUnmappedNodes(tmbSolution.buildDeactivatedNodes())
        );
    }

    private Best getBest(TMBSolution tmbSolution, double currentSpread) {
        double maxDelta = Double.NEGATIVE_INFINITY;
        int bestNode = -1;

        for (int node = 0; node < instance.getGraph().getNumNodes(); node++) {
            if (instance.getGraph().getSourcesSet().contains(node)) continue;
            if (tmbSolution.isDeactivated(node)) continue;

            tmbSolution.deactivate(node);
            diffusionModel.evaluate(tmbSolution);
            double newSpread = tmbSolution.getSpread();
            double delta = currentSpread - newSpread;

            if (delta > maxDelta) {
                maxDelta = delta;
                bestNode = node;
            }

            tmbSolution.activate(node); // restore state
        }

        if (bestNode == -1) {
            log.warn("No more nodes to deactivate, stopping.");
            return null;
        }

        return new Best(maxDelta, bestNode);
    }

    private static class Best {
        public final double maxDelta;
        public final int bestNode;

        public Best(double maxDelta, int bestNode) {
            this.maxDelta = maxDelta;
            this.bestNode = bestNode;
        }
    }

}
