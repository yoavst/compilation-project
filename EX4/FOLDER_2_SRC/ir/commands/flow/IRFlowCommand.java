package ir.commands.flow;

import ir.commands.IRCommand;
import utils.NotNull;

/**
 * Base class for flow command. Flow command may jump to {@link #getLabel()} if (possible empty) condition satisfies.
 */
public abstract class IRFlowCommand extends IRCommand {
    @NotNull
    private final IRLabel label;

    IRFlowCommand(@NotNull String description, @NotNull IRLabel label) {
        super(description);
        this.label = label;
    }

    @NotNull
    public IRLabel getLabel() {
        return label;
    }
}
