package ir.arithmetic;

import ir.IRAssignmentCommand;
import ir.registers.Register;
import utils.NotNull;

public class IRBinOpCommand extends IRAssignmentCommand {
    @NotNull
    private final Register first;
    private final Operation op;
    @NotNull
    private final Register second;

    public IRBinOpCommand(@NotNull Register dest, @NotNull Register first, Operation op, @NotNull Register second) {
        super("var1 := var2 op var3", dest);
        this.first = first;
        this.op = op;
        this.second = second;
    }

    @Override
    public String toString() {
        return String.format("%s := %s %s %s", dest, first, op.text, second);
    }

}
