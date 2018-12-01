package types.builtins;

import types.Type;

public final class TypeVoid extends Type {
    public static final TypeVoid instance = new TypeVoid();

    private TypeVoid() {
        super("void");
    }

    @Override
    public boolean isClass() {
        return false;
    }
}
