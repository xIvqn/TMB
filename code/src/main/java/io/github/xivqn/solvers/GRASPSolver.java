package io.github.xivqn.solvers;

import io.github.xivqn.diffmodels.DiffusionModel;
import io.github.xivqn.diffmodels.DiffusionModelFactory;
import io.github.xivqn.entities.Instance;
import io.github.xivqn.entities.Solution;
import io.github.xivqn.entities.TMBSolution;
import io.github.xivqn.entities.TabuMemo;
import io.github.xivqn.exceptions.ImportException;
import io.github.xivqn.exceptions.ModelException;
import io.github.xivqn.exceptions.SolverException;
import io.github.xivqn.utils.ArgsUtils;
import io.github.xivqn.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class GRASPSolver implements Solver {

    private static final Logger log = LoggerFactory.getLogger(GRASPSolver.class);

    private Instance instance;
    private DiffusionModel diffusionModel;
    private double gamma;
    private List<Integer> candidates;

    private final double tenureFactor;

    private final double alpha;
    private final int maxIterations;

    private final int[] deactivatedNodes, activatedNodes;
    private int deactivatedNodesSize, activatedNodesSize;

    public GRASPSolver(Instance instance, double gamma) throws ModelException, ImportException {
        long startInitializationTime = System.currentTimeMillis();
        this.instance = instance;
        this.diffusionModel = DiffusionModelFactory.getModel(ArgsUtils.getDiffusionModelType(), instance);
        this.gamma = gamma;
        this.candidates = buildCandidateList(instance);
        this.tenureFactor = ArgsUtils.getTenureFactor();
        this.alpha = ArgsUtils.getAlpha();
        this.maxIterations = ArgsUtils.getGRASPIterations();
        this.deactivatedNodes = new int[instance.getGraph().getNumNodes() * 2];
        this.activatedNodes = new int[instance.getGraph().getNumNodes() * 2];
        this.deactivatedNodesSize = 0;
        this.activatedNodesSize = 0;
        log.debug("TMB Solver for {} started in {} ms", instance.getName(), System.currentTimeMillis() - startInitializationTime);
    }

    @Override
    public Solution solve() throws SolverException {
        TMBSolution best = new TMBSolution(instance.getGraph(), true);
        long startTime = System.currentTimeMillis();
        long cutoff = startTime + ArgsUtils.getTimeLimit() * 1000L;
        double localAlpha = alpha;
        List<Integer> localCandidates = new LinkedList<>(candidates);

        for (int iterations = 1; iterations <= maxIterations && cutoff >= System.currentTimeMillis(); iterations++) {
            TMBSolution current = new TMBSolution(instance.getGraph());
            diffusionModel.evaluate(current);
            double currentSpread = current.getSpread(), bestSpread = current.getSpread();
            double targetSpread = currentSpread - currentSpread * gamma;
            localCandidates.clear();
            localCandidates.addAll(candidates);
            log.info("Iteration {}: Initial evaluation {}, target={} (gamma={})", iterations, currentSpread, targetSpread, gamma);

            // Construct solution
            while (currentSpread > targetSpread) {
                if (iterations == 1) localAlpha = 0.0;
                else if (alpha == -1.0) localAlpha = RandomUtils.getRandom().nextDouble();
                else localAlpha = alpha;

                int gMax = instance.getGraph().degreeOutOf(localCandidates.get(0));
                int gMin = instance.getGraph().degreeOutOf(localCandidates.get(localCandidates.size() - 1));
                int g = gMax - (int) ((gMax - gMin) * localAlpha);

                int rlcSize = 0;
                for (int i = 0; i < localCandidates.size(); i++) {
                    if (instance.getGraph().degreeOutOf(localCandidates.get(i)) >= g) rlcSize++;
                    else break;
                }

                int poolIndex = RandomUtils.getRandom().nextInt(rlcSize);
                int node = localCandidates.get(poolIndex);
                localCandidates.remove(poolIndex);

                current.deactivate(node);

                diffusionModel.evaluate(current);
                currentSpread = current.getSpread();
                bestSpread = Math.min(bestSpread, currentSpread);

                log.debug("Iteration {}: current={}, best={}, target={}, deactivated={}", iterations, currentSpread, bestSpread, targetSpread, current.getDeactivationCount());
            }

            // LS
            exchangeNodes(current, targetSpread);

            if (best.getDeactivationCount() > current.getDeactivationCount()) {
                // Update best Solution
                best.clearDeactivatedNodes();
                List<Integer> deactivatedNodes = current.buildDeactivatedNodes();
                for (int i = 0; i < deactivatedNodes.size(); i++) {
                    best.deactivate(deactivatedNodes.get(i));
                }
                best.setSpread(current.getSpread());
                log.info("New best solution {} with spread {}", best.getDeactivationCount(), best.getSpread());
            }

            if (best.getDeactivationCount() == 1) {
                log.info("Iteration {}: Optimal solution found with 1 deactivated node, stopping search.", iterations);
                break;
            }

            if (iterations < maxIterations && cutoff >= System.currentTimeMillis()) {
                localCandidates.clear();
                localCandidates.addAll(candidates);
                log.trace("Resetting for next iteration, current evaluation {}, deactivated={}", current.getSpread(), current.getDeactivationCount());
            }
        }

        long endTime = System.currentTimeMillis();

        return new Solution(
                instance.getName(),
                best.getSpread(),
                endTime - startTime,
                instance.getGraph().getUnmappedNodes(best.buildDeactivatedNodes())
        );
    }

    protected LinkedList<Integer> buildCandidateList(Instance instance) {
        LinkedList<Integer> nodes = new LinkedList<>();
        Set<Integer> sources = Arrays.stream(instance.getGraph().getSources()).boxed().collect(Collectors.toSet());

        for (int i = 0; i < instance.getGraph().getNumNodes(); i++)
            if (!sources.contains(i)) nodes.add(i);
        nodes.sort(Comparator.comparingInt((Integer node) -> instance.getGraph().degreeOutOf(node)).reversed());

        return nodes;
    }

    private void exchangeNodes(TMBSolution tmbSolution, double targetSpread) {
        boolean improve = true;
        long cutoff = System.currentTimeMillis() + ArgsUtils.getLsTimeLimit() * 1000L;

        // Nodes that have been removed recently should not be added immediately
        TabuMemo deletionTabu = new TabuMemo((int) (tenureFactor * tmbSolution.getDeactivationCount()));
        // Nodes that have been added recently should not be removed immediately
        TabuMemo additionTabu = new TabuMemo((int) (tenureFactor * tmbSolution.getDeactivationCount()));
        log.debug("Starting local search with tabu tenures: deletion={}, addition={}",
                deletionTabu.getTenure(), additionTabu.getTenure());

        while (improve && System.currentTimeMillis() < cutoff && tmbSolution.getDeactivationCount() > 1) {
            improve = false;
            deactivatedNodesSize = 0;
            activatedNodesSize = 0;
            for (int node = 0; node < instance.getGraph().getNumNodes(); node++) {
                if (instance.getGraph().getSourcesSet().contains(node)) continue;

                if (tmbSolution.isDeactivated(node)) {
                    deactivatedNodes[deactivatedNodesSize++] = node;
                } else {
                    activatedNodes[activatedNodesSize++] = node;
                }
            }

            for (int i = 0; i < activatedNodesSize && !improve; i++) {
                int u = activatedNodes[i];
                if (deletionTabu.isTabu(u)) continue;
                if (tmbSolution.getDeactivationCount() <= 1) break;

                for (int j = 0; j < deactivatedNodesSize && !improve; j++) {
                    int v = deactivatedNodes[j];
                    if (additionTabu.isTabu(v)) continue;
                    if (tmbSolution.getDeactivationCount() <= 1) break;

                    for (int k = j + 1; k < deactivatedNodesSize && !improve; k++) {
                        int w = deactivatedNodes[k];
                        if (additionTabu.isTabu(w)) continue;
                        if (tmbSolution.getDeactivationCount() <= 1) break;

                        if (System.currentTimeMillis() > cutoff) {
                            log.debug("LS execution time limit reached for {}", instance.getName());
                            return;
                        }

                        // Perform the 2-by-1 swap:
                        tmbSolution.deactivate(u);
                        tmbSolution.activate(v);
                        tmbSolution.activate(w);

                        diffusionModel.fastEvaluate(tmbSolution);
                        double newSpread = tmbSolution.getSpread();

                        if (newSpread <= targetSpread) {
                            diffusionModel.evaluate(tmbSolution);
                            if (tmbSolution.getSpread() <= targetSpread) {
                                // Successful exchange, update tabu lists
                                additionTabu.add(u);
                                deletionTabu.add(v);
                                deletionTabu.add(w);

                                log.debug("Exchanged activated node {} with deactivated nodes {} and {}, newSpread={}, deactivated={}",
                                        u, v, w, newSpread, tmbSolution.getDeactivationCount());
                                improve = true;
                            } else {
                                // Revert the changes
                                tmbSolution.activate(u);
                                tmbSolution.deactivate(v);
                                tmbSolution.deactivate(w);
                            }
                        } else {
                            // Revert the changes
                            tmbSolution.activate(u);
                            tmbSolution.deactivate(v);
                            tmbSolution.deactivate(w);
                        }
                    }
                }
            }
        }
    }
}
