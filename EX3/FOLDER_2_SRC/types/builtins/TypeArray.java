package types.builtins;

import types.Type;
import types.TypeError;
import utils.NotNull;

public class TypeArray extends Type {
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
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeArray && ((TypeArray) obj).name.equals(name) && ((TypeArray) obj).arrayType.equals(arrayType);
    }

    @Override
    public boolean isAssignableFrom(Type t) {
        return equals(t) || t == TypeNil.instance || (t instanceof TypeArrayUnspecified && ((TypeArrayUnspecified) t).arrayType == arrayType) || t == TypeError.instance;
    }
}
