package types;

import utils.NotNull;

/**
 * Type for propagating error in function declarations
 */
public final class TypeFunctionUnspecified extends TypeFunction {
    public TypeFunctionUnspecified(@NotNull String name) {
        super("Unspecified of " + name, TypeError.instance);
    }
}
