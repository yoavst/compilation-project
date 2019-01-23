package ir.flow;

import ir.IRCommand;
import utils.NotNull;

public class IRGotoCommand extends IRCommand {
    @NotNull
    private final IRLabel label;
    public IRGotoCommand(@NotNull IRLabel label) {
        super("goto label");
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("goto %s", label);
    }
}
