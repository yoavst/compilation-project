package types.builtins;

import types.Type;
import types.TypeError;

public final class TypeInt extends Type {
    public static final TypeInt instance = new TypeInt();

    private TypeInt() {
        super("int");
    }

    @Override
    public boolean isClass() {
        return false;
    }

    @Override
    public boolean canBeCastedToBoolean() {
        return true;
    }

    @Override
    public boolean isAssignableFrom(Type t) {
        return t == instance || t == TypeError.instance;
    }
}
