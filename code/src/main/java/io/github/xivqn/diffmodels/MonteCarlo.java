package io.github.xivqn.diffmodels;

import io.github.xivqn.entities.Graph;
import io.github.xivqn.entities.Instance;
import io.github.xivqn.entities.TMBSolution;
import io.github.xivqn.utils.ArgsUtils;
import io.github.xivqn.utils.ConcurrencyUtil;
import io.github.xivqn.utils.Parallel;
import io.github.xivqn.utils.RandomUtils;
import org.apache.commons.math3.random.MersenneTwister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public abstract class MonteCarlo implements DiffusionModel {

    protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    protected Instance instance;

    protected int activeNodes;
    protected double spread;

    protected boolean[] A, A_stored;
    protected int[] new_active, new_ones;

    protected final int simulations;

    private final boolean parallelEvaluation;

    private ExecutorService executor = Parallel.executor;

    public MonteCarlo(Instance instance) {
        this.instance = instance;
        this.A = new boolean[instance.getGraph().getNumNodes()];
        this.A_stored = new boolean[instance.getGraph().getNumNodes()];
        this.new_active = new int[instance.getGraph().getNumNodes()];
        this.new_ones = new int[instance.getGraph().getNumNodes()];
        this.activeNodes = 0;
        this.simulations = ArgsUtils.getSimulations();
        this.parallelEvaluation = ArgsUtils.isParallelEvaluation();
    }

    public void evaluate(TMBSolution tmbSolution) {
        if (parallelEvaluation) evaluateParallel(tmbSolution, simulations);
        else evaluateSequential(tmbSolution, simulations);
    }

    public void fastEvaluate(TMBSolution tmbSolution) {
        int fastSimulations = Math.max(Parallel.getAvailableThreads(), simulations / 1000);

        if (parallelEvaluation) evaluateParallel(tmbSolution, fastSimulations);
        else evaluateSequential(tmbSolution, fastSimulations);
    }

    public void evaluateSequential(TMBSolution tmbSolution, int simulations) {
        double sum = 0;
        for (int i = 0; i < simulations; i++) {
            int cnt_new_active = 0;
            int countAddsA = 0;
            Arrays.fill(A, false);
            Arrays.fill(A_stored, false);
            for (int j = 0; j < tmbSolution.getSources().length; j++) {
                int node = tmbSolution.getSources()[j];
                A[node] = true;
                A_stored[node] = true;
                new_active[cnt_new_active] = node;
                cnt_new_active += 1;
                countAddsA += 1;
            }
            countAddsA = calculateTotalActivation(tmbSolution, cnt_new_active, countAddsA, i);
            sum += countAddsA;
        }
        this.spread = sum / (double) simulations;
        this.activeNodes = (int) this.spread;
        tmbSolution.setSpread(spread);
    }

    public void evaluateParallel(TMBSolution tmbSolution, int simulations) {
        var futures = new ArrayList<Future<Integer>>();

        for (int i = 0; i < simulations; i++) {
            var cnt = i;
            var rnd = RandomUtils.getNew(i);
            futures.add(executor.submit(() -> singleEvaluation(instance, tmbSolution, cnt)));
        }

        var results = ConcurrencyUtil.awaitAll(futures);
        int suma = 0;

        for (int i = 0; i < results.size(); i++) {
            suma += results.get(i);
        }

        tmbSolution.setSpread((double) suma / (double) simulations);
    }

    public int singleEvaluation(Instance instance, TMBSolution tmbSolution, int id){
        boolean[] A_parallel = new boolean[instance.getGraph().getNumNodes()];
        boolean[] A_stored_parallel = new boolean[instance.getGraph().getNumNodes()];
        int[] new_active_parallel = new int[instance.getGraph().getNumNodes()];
        int[] new_ones_parallel = new int[instance.getGraph().getNumNodes()];

        int cnt_new_active = 0;
        int countAddsA = 0;

        for (int j = 0; j < tmbSolution.getSources().length; j++) {
            int node = tmbSolution.getSources()[j];
            A_parallel[node] = true;
            A_stored_parallel[node] = true;
            new_active_parallel[cnt_new_active] = node;
            cnt_new_active += 1;
            countAddsA += 1;
        }

        countAddsA = calculateTotalActivation(
                tmbSolution,
                id,
                cnt_new_active,
                countAddsA,
                new_active_parallel,
                new_ones_parallel,
                A_parallel,
                A_stored_parallel,
                instance.getGraph()
        );

        return countAddsA - tmbSolution.getSources().length;
    }

    protected abstract int calculateTotalActivation(TMBSolution tmbSolution, int id, int cnt_new_active, int countAddsA);

    protected abstract int calculateTotalActivation(
            TMBSolution tmbSolution, int id,
            int cnt_new_active, int countAddsA,
            int[] new_active, int[] new_ones,
            boolean[] A, boolean[] A_stored,
            Graph graph
    );

}
