package ir.commands.memory;

import ir.commands.IRAssignmentCommand;
import ir.commands.flow.IRLabel;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRLoadFromLabelCommand extends IRAssignmentCommand {
    @NotNull
    private IRLabel label;

    public IRLoadFromLabelCommand(@NotNull Register dest, @NotNull IRLabel label) {
        super("var1 := [variable]", dest);
        this.label = label;
    }

    @Override
    public Set<Register> getInvalidates() {
        return setOf(dest);
    }

    @Override
    public String toString() {
        return String.format("%s := [%s]", dest, label);
    }
}
