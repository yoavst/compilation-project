package ir.functions;

import ir.IRCommand;
import ir.flow.IRLabel;
import utils.NotNull;

public class IRLabelMark extends IRCommand {
    @NotNull
    private final IRLabel label;
    public IRLabelMark(@NotNull IRLabel label) {
        super("label:");
        this.label = label;
    }

    @NotNull
    public IRLabel getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return String.format("%s:", label);
    }
}
