package ir.commands;

import ir.registers.Register;
import utils.NotNull;

import java.util.Collections;
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

    /**
     * Returns the registers that are dependencies for this command
     * e.g. for binary operations, the operands are dependencies.
     */
    public Set<Register> getDependencies() {
        return Collections.emptySet();
    }

    /**
     * Returns the registers the command invalidates.
     * e.g. registers it changes their value
     */
    public Set<Register> getInvalidates() {
        return Collections.emptySet();
    }

    /**
     * Returns textual representation for the command
     */
    @Override
    public String toString() {
        return description;
    }

    /**
     * Returns description of the command for debug purposes
     */
    @NotNull
    public String getDescription() {
        return description;
    }
}
