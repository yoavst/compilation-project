package types.builtins;

import types.Type;
import utils.NotNull;

public final class TypeArrayUnspecified extends TypeArray {
    public TypeArrayUnspecified(@NotNull Type arrayType) {
        super("Unspecified of " + arrayType, arrayType);
    }


}
