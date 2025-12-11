package io.github.xivqn.solvers;

import io.github.xivqn.entities.Instance;
import io.github.xivqn.exceptions.ImportException;
import io.github.xivqn.exceptions.ModelException;
import io.github.xivqn.importers.PageRankImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PageRankSolver extends PrioritySolver {

    private static final Logger log = LoggerFactory.getLogger(PageRankSolver.class);

    public PageRankSolver(Instance instance, double gamma) throws ModelException, ImportException {
        super(instance, gamma);
    }

    protected List<Integer> buildPool(Instance instance) throws ImportException {
        List<Double> pageRankList;
        try {
            pageRankList = PageRankImporter.importPageRanks(instance.getName(), instance.getGraph().getNumNodes());
        } catch (IOException e) {
            throw new ImportException("PageRank file does not exist or it's not accessible.", e);
        }

        List<Integer> nodes = new ArrayList<>(instance.getGraph().getNumNodes());

        for (int i = 0; i < instance.getGraph().getNumNodes(); i++)
            if (!instance.getGraph().getSourcesSet().contains(i)) nodes.add(i);
        nodes.sort(Comparator.comparingDouble(pageRankList::get));

        return nodes;
    }

}
