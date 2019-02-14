package ir.commands.flow;

import ir.commands.IRCommand;
import utils.NotNull;

/**
 * Represents a label in the IR. Each label should have a unique name.
 */
public final class IRLabel extends IRCommand {
    @NotNull
    private final String name;
    private boolean isStartingLabel;

    public IRLabel(@NotNull String name) {
        super("label:");
        this.name = name;
    }

    public boolean isStartingLabel() {
        return isStartingLabel;
    }

    public IRLabel startingLabel() {
        isStartingLabel = true;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof IRLabel && ((IRLabel) o).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
