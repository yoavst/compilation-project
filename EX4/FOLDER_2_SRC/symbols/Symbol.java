package symbols;

import types.Type;
import types.TypeClass;
import types.TypeFunction;
import utils.NotNull;
import utils.Nullable;

public class Symbol {
    private static final String SYMBOL_SIGN = "_";
    @NotNull
    private final String name;
    @NotNull
    public final Type type;
    @Nullable
    public final TypeClass instance;

    public Symbol(@NotNull String name, @NotNull Type type, @Nullable TypeClass instance) {
        this.name = name;
        this.type = type;
        this.instance = instance;
    }

    public Symbol(@NotNull String name, @NotNull Type type) {
        this(name, type, null);
    }

    @Override
    public String toString() {
        if (instance == null)
            return SYMBOL_SIGN + name;
        else
            return instance.name + SYMBOL_SIGN + name;
    }

    public boolean isFunction() {
        return type instanceof TypeFunction;
    }

    @NotNull
    public TypeFunction getFunction() {
        assert isFunction();
        return (TypeFunction) type;
    }

    public boolean isField() {
        return !isFunction();
    }

    public boolean isBounded() {
        return instance != null;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Symbol))
            return false;

        Symbol s = (Symbol) o;
        return s.name.equals(name) &&
                s.type.equals(type) &&
                ((s.instance == null && instance == null) ||
                        (s.instance != null && instance != null && (s.instance.isAssignableFrom(instance) || instance.isAssignableFrom(s.instance))));
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
