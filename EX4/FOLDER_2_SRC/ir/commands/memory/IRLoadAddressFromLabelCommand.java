package ir.commands.memory;

import ir.commands.IRAssignmentCommand;
import ir.commands.flow.IRLabel;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRLoadAddressFromLabelCommand extends IRAssignmentCommand {
    @NotNull
    public IRLabel label;

    public IRLoadAddressFromLabelCommand(@NotNull Register dest, @NotNull IRLabel label) {
        super("var1 := addr [variable]", dest);
        this.label = label;
    }

    @Override
    public Set<Register> getInvalidates() {
        return setOf(dest);
    }

    @Override
    public String toString() {
        return String.format("%s := addr [%s]", dest, label);
    }
}
