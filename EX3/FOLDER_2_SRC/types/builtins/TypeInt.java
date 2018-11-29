package types.builtins;

import types.Type;

public final class TypeInt extends Type {
    public static final TypeInt instance = new TypeInt();

    private TypeInt() {
        super("int");
    }

    @Override
    public boolean isClass() {
        return true;
    }
}
