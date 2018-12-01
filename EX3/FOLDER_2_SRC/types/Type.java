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

    public Type(@NotNull String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "<" + name + ">";
    }
}
