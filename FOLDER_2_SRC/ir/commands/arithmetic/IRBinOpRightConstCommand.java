package ir.commands.arithmetic;

import ir.commands.IRAssignmentCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRBinOpRightConstCommand extends IRAssignmentCommand {
    @NotNull
    public final Register first;
    public final Operation op;
    public final int second;

    public IRBinOpRightConstCommand(@NotNull Register dest, @NotNull Register first, Operation op, int second) {
        super("var1 := var2 op var3", dest);
        this.first = first;
        this.op = op;
        this.second = second;
        this.dependencies = setOf(first);
        this.invalidates = setOf(dest);
    }

    @Override
    public String toString() {
        return String.format("%s := %s %s %d", dest, first, op.text, second);
    }

    @Override
    public boolean canBeOptimized() {
        return !dest.isGlobal();
    }

}
