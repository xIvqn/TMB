package io.github.xivqn.diffmodels;

import io.github.xivqn.entities.TMBSolution;

public interface DiffusionModel {

    void evaluate(TMBSolution tmbSolution);

    void fastEvaluate(TMBSolution tmbSolution);

}
