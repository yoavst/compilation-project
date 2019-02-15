package ir.commands.arithmetic;

import ir.commands.IRAssignmentCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRBinOpCommand extends IRAssignmentCommand {
    @NotNull
    public final Register first;
    public final Operation op;
    @NotNull
    public final Register second;

    public IRBinOpCommand(@NotNull Register dest, @NotNull Register first, Operation op, @NotNull Register second) {
        super("var1 := var2 op var3", dest);
        this.first = first;
        this.op = op;
        this.second = second;
    }

    @Override
    public Set<Register> getDependencies() {
        return setOf(first, second);
    }

    @Override
    public Set<Register> getInvalidates() {
        return setOf(dest);
    }

    @Override
    public String toString() {
        return String.format("%s := %s %s %s", dest, first, op.text, second);
    }

}
