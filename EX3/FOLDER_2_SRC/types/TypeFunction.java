package types;

import types.builtins.TypeVoid;
import utils.NotNull;

import java.util.Collections;
import java.util.List;

public class TypeFunction extends Type {
    @NotNull
    public final Type returnType;

    @NotNull
    public final List<Type> params;


    public TypeFunction(String name, @NotNull Type returnType, @NotNull List<Type> params) {
        super(name);
        this.returnType = returnType;
        this.params = params;
    }

    public TypeFunction(@NotNull String name, @NotNull Type returnType) {
        this(name, returnType, Collections.emptyList());
    }

    public TypeFunction(@NotNull String name) {
        this(name, TypeVoid.instance);
    }

    @Override
    public boolean isFunction() { return true; }

    @Override
    public boolean isAssignableFrom(Type t) {
       throw new RuntimeException(new IllegalAccessException("Should not be called"));
    }
}
