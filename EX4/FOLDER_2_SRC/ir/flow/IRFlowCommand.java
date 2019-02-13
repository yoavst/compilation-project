package ir.flow;

import ir.IRCommand;
import utils.NotNull;

public class IRFlowCommand extends IRCommand {
    @NotNull
    protected final IRLabel label;

    public IRFlowCommand(@NotNull String description, @NotNull IRLabel label) {
        super("ifz var1 goto label");
        this.label = label;
    }

    public IRLabel getLabel() {
        return label;
    }
}
