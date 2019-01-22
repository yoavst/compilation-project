package ir.flow;

import ir.IRCommand;
import utils.NotNull;

public class IRGotoCommand extends IRCommand {
    @NotNull
    private final String label;
    public IRGotoCommand(@NotNull String label) {
        super("goto label");
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("goto %s", label);
    }
}
