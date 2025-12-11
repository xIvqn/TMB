package io.github.xivqn.diffmodels;

import io.github.xivqn.entities.Instance;
import io.github.xivqn.exceptions.ModelException;
import io.github.xivqn.utils.ArgsUtils;

public class DiffusionModelFactory {

    /**
     * Returns a new DiffusionModel of the given type
     * @param type The type of model to create
     * @param instance The instance to solve
     * @return A new DM solver
     * @throws ModelException If the model type is unknown
     */
    public static DiffusionModel getModel(DiffusionModelTypes type, Instance instance) throws ModelException {
        switch (type) {
            case IC:
                return new ICModel(instance);
            case LT:
                return new LTModel(instance);
            default:
                throw new ModelException("Unknown diffusion model type");
        }
    }

    public static String getModelDescription(DiffusionModelTypes type) {
        switch (type) {
            case IC:
                return String.format("_IC_p=%s_sims=%s", ArgsUtils.getP(), ArgsUtils.getSimulations());
            case LT:
                return String.format("_LT_sims=%s", ArgsUtils.getSimulations());
            default:
                throw new ModelException("Unknown diffusion model type");
        }
    }
}
