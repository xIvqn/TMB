package io.github.xivqn.solvers;

import io.github.xivqn.entities.Instance;
import io.github.xivqn.exceptions.ImportException;
import io.github.xivqn.utils.ArgsUtils;

public class STMBFactory {

    public static Solver getSolver(Instance instance, double gamma) throws ImportException {
        switch (ArgsUtils.getDiffusionModelType()) {
            case IC: return new STMBICSolver(instance, gamma);
            case LT: return new STMBLTSolver(instance, gamma);
            default: throw new ImportException("Unknown diffusion model type.");
        }
    }
}
