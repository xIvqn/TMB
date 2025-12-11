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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PrioritySolver implements Solver {

    protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private Instance instance;
    private DiffusionModel diffusionModel;
    private double gamma;
    private List<Integer> pool;

    public PrioritySolver(Instance instance, double gamma) throws ModelException, ImportException {
        long startInitializationTime = System.currentTimeMillis();
        this.instance = instance;
        this.diffusionModel = DiffusionModelFactory.getModel(ArgsUtils.getDiffusionModelType(), instance);
        this.gamma = gamma;
        this.pool = buildPool(instance);
        log.debug("TMB Solver for {} started in {} ms", instance.getName(), System.currentTimeMillis() - startInitializationTime);
    }

    @Override
    public Solution solve() throws SolverException {
        TMBSolution tmbSolution = new TMBSolution(instance.getGraph());
        long startTime = System.currentTimeMillis();
        diffusionModel.evaluate(tmbSolution);
        double currentSpread = tmbSolution.getSpread(), bestSpread = tmbSolution.getSpread();
        double targetSpread = currentSpread - currentSpread * gamma;
        int poolIndex = 0, iterations = 0;
        log.info("Initial evaluation {}, target={} (gamma={})", currentSpread, targetSpread, gamma);

        while (currentSpread > targetSpread) {
            int node = pool.get(poolIndex++);
            iterations++;

            tmbSolution.deactivate(node);

            diffusionModel.evaluate(tmbSolution);
            currentSpread = tmbSolution.getSpread();
            bestSpread = Math.min(bestSpread, currentSpread);

            log.debug("Iteration {}: current={}, best={}, target={}, deactivated={}", iterations, currentSpread, bestSpread, targetSpread, tmbSolution.getDeactivationCount());
        }

        long endTime = System.currentTimeMillis();

        return new Solution(
                instance.getName(),
                tmbSolution.getSpread(),
                endTime - startTime,
                instance.getGraph().getUnmappedNodes(tmbSolution.buildDeactivatedNodes())
        );
    }

    protected abstract List<Integer> buildPool(Instance instance) throws ImportException;

}
