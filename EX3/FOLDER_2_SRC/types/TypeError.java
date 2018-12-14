package types;

import utils.NotNull;

public final class TypeError extends Type {
    @NotNull
    public static final TypeError instance = new TypeError();

    private TypeError() {
        super("[ERROR]");
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isAssignableFrom(Type t) {
        return true;
    }
}
