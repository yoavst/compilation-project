package ir.commands;

import ir.registers.Register;
import utils.NotNull;

/**
 * Base class for assignment command. Each assignment command has a destination register.
 * <br>
 * Note: the value may not be stored in dest, e.g. in [dest] instead.
 */
public abstract class IRAssignmentCommand extends IRCommand {
    @NotNull
    public final Register dest;

    public IRAssignmentCommand(@NotNull String description, @NotNull Register dest) {
        super(description);
        this.dest = dest;
    }
}
