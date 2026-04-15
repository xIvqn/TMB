package io.github.xivqn.diffmodels;

import io.github.xivqn.entities.Graph;
import io.github.xivqn.entities.Instance;
import io.github.xivqn.entities.TMBSolution;
import io.github.xivqn.utils.ArgsUtils;
import io.github.xivqn.utils.RandomUtils;
import org.apache.commons.math3.random.MersenneTwister;

public class ICModel extends MonteCarlo {

    protected final MersenneTwister rand;
    private final double p;

    public ICModel(Instance instance) {
        super(instance);
        this.rand = RandomUtils.getRandom();
        this.p = ArgsUtils.getP();
    }

    protected int calculateTotalActivation(
            TMBSolution tmbSolution,
            int id,
            int cnt_new_active, int countAddsA,
            int[] new_active, int[] new_ones,
            boolean[] A, boolean[] A_stored,
            Graph graph
    ) {
        MersenneTwister rand = RandomUtils.getNew(id);
        return calculateTotalActivation(
                tmbSolution, id, cnt_new_active, countAddsA, new_active, new_ones, A, A_stored, graph, rand
        );
    }

    private int calculateTotalActivation(
            TMBSolution tmbSolution,
            int id,
            int cnt_new_active, int countAddsA,
            int[] new_active, int[] new_ones,
            boolean[] A, boolean[] A_stored,
            Graph graph,
            MersenneTwister rand
    ) {
        while (cnt_new_active != 0) {
            int cnt_new_ones = 0;
            //For each newly active node, find its neighbors that become activated
            for(int m = 0; m < cnt_new_active; m++) {
                int node = new_active[m];
                //Get random list of values [0,1]
                for (int cnt = 0; cnt < graph.degreeOutOf(node); cnt += 1) {
                    int count = graph.getAdjacentsOut(node).get(cnt);
                    double v = rand.nextDouble();
                    if (tmbSolution.isDeactivated(count)) continue;
                    if (v <= this.p) {
                        if (!A[count]) {
                            A[count] = true;
                            A_stored[count] = true;
                            new_ones[cnt_new_ones] = count;
                            cnt_new_ones += 1;
                            countAddsA += 1;
                        }
                    }
                }
            }
            cnt_new_active = cnt_new_ones;
            for (int j = 0; j < cnt_new_ones; j++) {
                new_active[j] = new_ones[j];
            }
        }

        this.log.trace("Simulation {} achieved {}", id, countAddsA);

        return countAddsA;
    }

    protected int calculateTotalActivation(TMBSolution tmbSolution, int id, int cnt_new_active, int countAddsA) {
        return calculateTotalActivation(
                tmbSolution, id, cnt_new_active, countAddsA,
                super.new_active, super.new_ones, super.A, super.A_stored, super.instance.getGraph(), this.rand
        );
    }

}
