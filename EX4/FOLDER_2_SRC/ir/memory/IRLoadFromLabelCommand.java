package ir.memory;

import ir.IRAssignmentCommand;
import ir.flow.IRLabel;
import ir.registers.Register;
import utils.NotNull;

public class IRLoadFromLabelCommand extends IRAssignmentCommand {
    @NotNull
    private IRLabel label;
    public IRLoadFromLabelCommand(@NotNull Register dest, @NotNull IRLabel label) {
        super("var1 := [variable]", dest);
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("%s := [%s]", dest, label);
    }
}
