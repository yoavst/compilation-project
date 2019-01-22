package ir.arithmetic;

import ir.IRAssignmentCommand;
import utils.NotNull;

public class IRBinOpRightConstCommand extends IRAssignmentCommand {
    @NotNull
    private final String first;
    private final Operation op;
    private final int second;

    public IRBinOpRightConstCommand(@NotNull String dest, @NotNull String first, Operation op, int second) {
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
