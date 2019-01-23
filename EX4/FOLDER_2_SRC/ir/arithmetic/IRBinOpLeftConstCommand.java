package ir.arithmetic;

import ir.IRAssignmentCommand;
import ir.Register;
import utils.NotNull;

public class IRBinOpLeftConstCommand extends IRAssignmentCommand {
    private final int first;
    private final Operation op;
    @NotNull
    private final Register second;

    public IRBinOpLeftConstCommand(@NotNull Register dest, int first, Operation op, @NotNull Register second) {
        super("var1 := var2 op var3", dest);
        this.first = first;
        this.op = op;
        this.second = second;
    }

    @Override
    public String toString() {
        return String.format("%s := %d %s %s", dest, first, op.text, second);
    }

}
