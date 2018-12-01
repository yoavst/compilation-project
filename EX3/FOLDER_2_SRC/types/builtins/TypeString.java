package types.builtins;

import types.Type;

public final class TypeString extends Type {
    public static final TypeString instance = new TypeString();

    private TypeString() {
        super("string");
    }

    @Override
    public boolean isClass() {
        return false;
    }

    @Override
    public boolean isAssignableFrom(Type t) {
        return t == instance;
    }
}
