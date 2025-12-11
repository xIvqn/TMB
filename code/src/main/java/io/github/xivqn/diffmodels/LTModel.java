package io.github.xivqn.diffmodels;

import io.github.xivqn.entities.Graph;
import io.github.xivqn.entities.Instance;
import io.github.xivqn.entities.TMBSolution;

public class LTModel extends MonteCarlo {

    private final int[] activeNeighbors;

    public LTModel(Instance instance) {
        super(instance);
        this.activeNeighbors = new int[instance.getGraph().getNumNodes()];
    }

    protected int calculateTotalActivation(
            TMBSolution tmbSolution,
            int id,
            int cnt_new_active, int countAddsA,
            int[] new_active, int[] new_ones,
            boolean[] A, boolean[] A_stored,
            Graph graph
    ) {
        int[] activeNeighbors = new int[graph.getNumNodes()];
        return calculateTotalActivation(
                tmbSolution, id, cnt_new_active, countAddsA, new_active, new_ones, A, A_stored, graph, activeNeighbors
        );
    }

    private int calculateTotalActivation(
            TMBSolution tmbSolution,
            int id,
            int cnt_new_active, int countAddsA,
            int[] new_active, int[] new_ones,
            boolean[] A, boolean[] A_stored,
            Graph graph,
            int[] activeNeighbors
    ) {
        while (cnt_new_active != 0) {
            int cnt_new_ones = 0;
            //For each newly active node, find its neighbors that become activated
            for(int m = 0; m < cnt_new_active; m++) {
                int node = new_active[m];
                //Get random list of values [0,1]
                for (int cnt = 0; cnt < graph.degreeOutOf(node); cnt += 1) {
                    int count = graph.getAdjacentsOut(node).get(cnt);
                    if (tmbSolution.isDeactivated(count)) continue;

                    activeNeighbors[count]++;

                    int numActiveNeighbors = activeNeighbors[count];
                    double activeWeight = graph.getInLTValue(count) * numActiveNeighbors;

                    if (activeWeight >= graph.getProbability(count)) {
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
                super.new_active, super.new_ones, super.A, super.A_stored, super.instance.getGraph(), this.activeNeighbors
        );
    }

}
