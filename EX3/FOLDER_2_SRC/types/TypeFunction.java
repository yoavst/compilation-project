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
    public boolean equals(Object obj) {
        return obj instanceof TypeFunction && ((TypeFunction) obj).name.equals(name) && sameSignature((TypeFunction) obj);
    }

    @Override
    public boolean isAssignableFrom(Type t) {
       throw new RuntimeException(new IllegalAccessException("Should not be called"));
    }

    /**
     * Check if the two functions has the same signature
     */
    public boolean sameSignature(TypeFunction func) {
        if (func instanceof TypeFunctionUnspecified || this instanceof TypeFunctionUnspecified)
            return true;

        if (func.params.size() != params.size() || !func.returnType.equals(returnType))
            return false;

        for (int i = 0; i < func.params.size(); i++) {
            if (!params.get(i).equals(func.params.get(i)))
                return false;
        }
        return true;
    }
}
