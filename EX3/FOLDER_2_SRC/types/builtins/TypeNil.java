package types.builtins;

import types.Type;

public class TypeNil extends Type {
    public static final TypeNil instance = new TypeNil();

    private TypeNil() {
        super("nil");
    }

    @Override
    public boolean isAssignableFrom(Type t) {
        // nil can only be replaced with nil.
        return t == instance;
    }
}
