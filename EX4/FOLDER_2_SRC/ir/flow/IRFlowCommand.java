package ir.flow;

import ir.IRCommand;
import utils.NotNull;

public abstract class IRFlowCommand extends IRCommand {
    @NotNull
    protected final IRLabel label;

    public IRFlowCommand(@NotNull String description, @NotNull IRLabel label) {
        super(description);
        this.label = label;
    }

    public IRLabel getLabel() {
        return label;
    }
}
