package io.github.xivqn.solvers;

import io.github.xivqn.entities.Instance;
import io.github.xivqn.exceptions.ImportException;
import io.github.xivqn.exceptions.ModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DegreeSolver extends PrioritySolver {

    private static final Logger log = LoggerFactory.getLogger(DegreeSolver.class);

    public DegreeSolver(Instance instance, double gamma) throws ModelException, ImportException {
        super(instance, gamma);
    }

    protected List<Integer> buildPool(Instance instance) {
        List<Integer> nodes = new ArrayList<>(instance.getGraph().getNumNodes());

        for (int i = 0; i < instance.getGraph().getNumNodes(); i++)
            if (!instance.getGraph().getSourcesSet().contains(i)) nodes.add(i);
        nodes.sort(Comparator.comparingInt((Integer node) -> instance.getGraph().degreeOutOf(node)).reversed());

        return nodes;
    }

}
