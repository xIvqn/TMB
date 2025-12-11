package io.github.xivqn.diffmodels;

import java.util.Arrays;

public enum DiffusionModelTypes {

    IC,
    LT,
    ;

    /**
     * Returns the string values of the enum
     * @return The string values of the enum
     */
    public static String[] stringValues() {
        return Arrays.stream(DiffusionModelTypes.values())
                .map(Enum::name)
                .toArray(String[]::new);
    }
}
