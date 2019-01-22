package ir.functions;

import ir.IRCommand;
import utils.NotNull;

public class IRCallCommand extends IRCommand {
    @NotNull
    private final String label;
    public IRCallCommand(@NotNull String label) {
        super("call label");
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("call %s", label);
    }
}
