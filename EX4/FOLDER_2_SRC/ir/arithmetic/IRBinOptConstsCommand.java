package ir.arithmetic;

import ir.IRAssignmentCommand;
import ir.Register;
import utils.NotNull;

public class IRBinOptConstsCommand extends IRAssignmentCommand {
    private final int first;
    private final Operation op;
    private final int second;

    public IRBinOptConstsCommand(@NotNull Register dest, int first, Operation op, int second) {
        super("var1 := var2 op var3", dest);
        this.first = first;
        this.op = op;
        this.second = second;
    }

    @Override
    public String toString() {
        return String.format("%s := %d %s %d", dest, first, op.text, second);
    }

}
