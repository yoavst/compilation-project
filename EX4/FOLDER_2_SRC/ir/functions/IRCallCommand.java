package ir.functions;

import ir.IRCommand;
import ir.flow.IRLabel;
import utils.NotNull;

public class IRCallCommand extends IRCommand {
    @NotNull
    private final IRLabel label;
    public IRCallCommand(@NotNull IRLabel label) {
        super("call label");
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("call %s", label);
    }
}
