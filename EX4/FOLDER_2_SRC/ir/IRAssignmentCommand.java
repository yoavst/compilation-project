package ir;

import ir.registers.Register;
import utils.NotNull;

public abstract class IRAssignmentCommand extends IRCommand {
    @NotNull
    protected final Register dest;

    public IRAssignmentCommand(@NotNull String description, @NotNull Register dest) {
        super(description);
        this.dest = dest;
    }
}
