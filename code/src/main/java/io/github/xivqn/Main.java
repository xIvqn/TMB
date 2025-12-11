package io.github.xivqn;

import io.github.xivqn.exceptions.ExportException;
import io.github.xivqn.exceptions.ImportException;
import io.github.xivqn.exceptions.SolverException;
import io.github.xivqn.runners.TMBRunner;
import io.github.xivqn.utils.ArgsUtils;

public class Main {

    public static void main(String[] args) {
        ArgsUtils.buildArgs(args);

        //  Run the TMBRunner
        try {
            TMBRunner.process();
        } catch (ExportException | ImportException | SolverException e) {
            throw new RuntimeException(e);
        }
    }

}