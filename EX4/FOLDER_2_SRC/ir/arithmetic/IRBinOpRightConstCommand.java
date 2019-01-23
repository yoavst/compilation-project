package ir.arithmetic;

import ir.IRAssignmentCommand;
import ir.Register;
import utils.NotNull;

public class IRBinOpRightConstCommand extends IRAssignmentCommand {
    @NotNull
    private final Register first;
    private final Operation op;
    private final int second;

    public IRBinOpRightConstCommand(@NotNull Register dest, @NotNull Register first, Operation op, int second) {
        super("var1 := var2 op var3", dest);
        this.first = first;
        this.op = op;
        this.second = second;
    }

    @Override
    public String toString() {
        return String.format("%s := %s %s %d", dest, first, op.text, second);
    }

}
