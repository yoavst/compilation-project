package types;

import utils.NotNull;

public final class TYPE_FOR_SCOPE_BOUNDARIES extends Type {
    @NotNull
    public final Scope scope;
    public TYPE_FOR_SCOPE_BOUNDARIES(String name, @NotNull Scope scope) {
        super(name);
        this.scope = scope;
    }

    @Override
    public boolean isAssignableFrom(Type t) {
        throw new RuntimeException(new IllegalAccessException("Should not be called"));
    }

    public enum Scope {
        ClassScan, Class, Function, Block
    }
}
