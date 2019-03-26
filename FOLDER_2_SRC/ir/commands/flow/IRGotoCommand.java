package ir.commands.flow;

import utils.NotNull;

public class IRGotoCommand extends IRFlowCommand {
    public IRGotoCommand(@NotNull IRLabel label) {
        super("goto label", label);
    }

    @Override
    public String toString() {
        return String.format("goto %s", getLabel());
    }
}
