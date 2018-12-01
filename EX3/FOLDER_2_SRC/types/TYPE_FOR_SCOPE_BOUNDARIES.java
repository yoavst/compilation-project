package types;

import utils.NotNull;

public class TYPE_FOR_SCOPE_BOUNDARIES extends Type {
    @NotNull
    public final Scope scope;
    public TYPE_FOR_SCOPE_BOUNDARIES(String name, @NotNull Scope scope) {
        super(name);
        this.scope = scope;
    }

    public enum Scope {
        ClassScan, Class, Function, Block
    }
}
