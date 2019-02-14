package ir;

import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

/**
 * The base class of IR commands.
 */
public abstract class IRCommand {
    @NotNull
    private final String description;

    public IRCommand(@NotNull String description) {
        this.description = description;
    }

    public abstract Set<Register> getDependencies();
    public abstract Set<Register> getInvalidates();

    @Override
    public String toString() {
        return description;
    }

    public String getDescription() {
        return description;
    }
}
