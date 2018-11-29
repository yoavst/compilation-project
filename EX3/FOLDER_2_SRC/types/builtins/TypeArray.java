package types.builtins;

import types.Type;
import utils.NotNull;

public final class TypeArray extends Type {
    @NotNull
    public final Type arrayType;

    public TypeArray(@NotNull String name, @NotNull Type arrayType) {
        super(name);
        this.arrayType = arrayType;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isClass() {
        return true;
    }
}
