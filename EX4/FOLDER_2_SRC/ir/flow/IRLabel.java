package ir.flow;

import ir.IRCommand;
import utils.NotNull;

public final class IRLabel extends IRCommand {
    @NotNull
    private final String name;

    public IRLabel(@NotNull String name) {
        super("label:");
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IRLabel label = (IRLabel) o;

        return name.equals(label.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
