package ir.arithmetic;

import ir.IRAssignmentCommand;
import utils.NotNull;

public class IRBinOpLeftConstCommand extends IRAssignmentCommand {
    private final int first;
    private final Operation op;
    @NotNull
    private final String second;

    public IRBinOpLeftConstCommand(@NotNull String dest, int first, Operation op, @NotNull String second) {
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
