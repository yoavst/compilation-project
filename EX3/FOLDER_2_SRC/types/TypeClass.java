package types;

import types.builtins.TypeNil;
import utils.NotNull;
import utils.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The type representing a class.
 * Each class has a unique name and may have a parent class it extends.
 * Every class has its fields & methods in addition to the methods it derives from its parent.
 */
public final class TypeClass extends Type {
    /**
     * Parent class for the type or null.
     * <br><br>
     * <b>Note:</b> Unlike normal programming languages, Poseidon does not have a common
     * parent classes like {@link Object} in java.
     */
    @Nullable
    public final TypeClass parent;

    /**
     * The fields that were declared on this class.
     * <br><br>
     * <b>Note:</b> On performing lookup, make sure to check the fields of {@link #parent}.
     * This mapping contains only the current class' fields.
     */
    @NotNull
    private final Map<String, Type> fields;

    /**
     * The methods that were declared on this class.
     * <br><br>
     * <b>Note:</b> On performing lookup, make sure to check the methods of {@link #parent}.
     * This mapping contains only the current class' methods.
     */
    @NotNull
    private final Map<String, TypeFunction> methods;


    public TypeClass(String name, @Nullable TypeClass parent, @NotNull Map<String, Type> fields, @NotNull Map<String, TypeFunction> methods) {
        super(name);

        this.parent = parent;
        this.fields = fields;
        this.methods = methods;
    }

    public TypeClass(@NotNull String name, @Nullable TypeClass parent) {
        this(name, parent, new HashMap<>(), new HashMap<>());
    }

    public TypeClass(@NotNull String name) {
        this(name, null);
    }

    @Override
    public boolean isClass() {
        return true;
    }

    /**
     * Try to register method of the following name.
     *
     * @param name     The name of the method
     * @param function The type info of the function
     * @return True if operation succeed, false if the key is already registered.
     */
    public boolean registerMethod(@NotNull String name, @NotNull TypeFunction function) {
        if (methods.containsKey(name)) {
            return false;
        }

        methods.put(name, function);
        return true;
    }

    /**
     * Try to register field of the following name.
     *
     * @param name The name of the field
     * @param type The type info of the field
     * @return True if operation succeed, false if the key is already registered.
     */
    public boolean registerField(@NotNull String name, @NotNull Type type) {
        if (fields.containsKey(name)) {
            return false;
        }

        fields.put(name, type);
        return true;
    }

    /**
     * Tries to query a field by its name, returning null if not exist.
     */
    @Nullable
    public Type queryField(@NotNull String name) {
        return fields.get(name);
    }

    /**
     * Tries to query field recursively by its name, return null if not exist on this class and its parents.
     */
    @Nullable
    public Type queryFieldRecursively(@NotNull String name) {
        TypeClass currentType = this;
        while (currentType != null) {
            Type field = currentType.queryField(name);
            if (field != null) {
                return field;
            }
            currentType = currentType.parent;
        }
        return null;
    }

    /**
     * Tries to query a method by its name, returning null if not exist.
     */
    @Nullable
    public TypeFunction queryMethod(@NotNull String name) {
        return methods.get(name);
    }

    /**
     * Tries to query method recursively by its name, return null if not exist on this class and its parents.
     */
    @Nullable
    public TypeFunction queryMethodRecursively(@NotNull String name) {
        TypeClass currentType = this;
        while (currentType != null) {
            TypeFunction method = currentType.queryMethod(name);
            if (method != null) {
                return method;
            }
            currentType = currentType.parent;
        }
        return null;
    }

    /**
     * Check if t extends this class
     */
    @Override
    public boolean isAssignableFrom(Type t) {
        if (t == TypeNil.instance) return true;
        else if (t == TypeError.instance) return true;
        else if (!t.isClass()) return false;
        TypeClass checkedClass = (TypeClass) t;
        while (checkedClass != null) {
            if (checkedClass.equals(this))
                return true;
            checkedClass = checkedClass.parent;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeClass && ((TypeClass) obj).name.equals(name);
    }
}
