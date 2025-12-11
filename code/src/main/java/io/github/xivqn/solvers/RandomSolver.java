package io.github.xivqn.solvers;

import io.github.xivqn.exceptions.ImportException;
import io.github.xivqn.exceptions.ModelException;
import io.github.xivqn.entities.Instance;
import io.github.xivqn.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class RandomSolver extends PrioritySolver {

    private static final Logger log = LoggerFactory.getLogger(RandomSolver.class);

    public RandomSolver(Instance instance, double gamma) throws ModelException, ImportException {
        super(instance, gamma);
    }

    protected List<Integer> buildPool(Instance instance) {
        List<Integer> nodeList = new ArrayList<>(instance.getGraph().getNumNodes());

        for (int i = 0; i < instance.getGraph().getNumNodes(); i++)
            if (!instance.getGraph().getSourcesSet().contains(i)) nodeList.add(i);
        RandomUtils.shuffleList(nodeList);

        return nodeList;
    }

}
