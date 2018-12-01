package types;

import utils.NotNull;

/**
 * The basic common type class.
 */
public abstract class Type {
    @NotNull
    public String name;

    public boolean isClass() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isFunction() { return false; }

    public Type(@NotNull String name) {
        this.name = name;
    }

    /**
     * Return whether in every place a `this` type is expected, one can put an instance of `t` instead.
     */
    public abstract boolean isAssignableFrom(Type t);

    @Override
    public String toString() {
        return "<" + name + ">";
    }
}
