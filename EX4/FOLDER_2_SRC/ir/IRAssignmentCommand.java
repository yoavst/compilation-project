package ir;

import utils.NotNull;

public abstract class IRAssignmentCommand extends IRCommand {
    @NotNull
    protected final String dest;

    public IRAssignmentCommand(@NotNull String description, @NotNull String dest) {
        super(description);
        this.dest = dest;
    }
}
