package ir.arithmetic;

import ir.IRAssignmentCommand;
import utils.NotNull;

public class IRBinOpCommand extends IRAssignmentCommand {
    @NotNull
    private final String first;
    private final Operation op;
    @NotNull
    private final String second;

    public IRBinOpCommand(@NotNull String dest, @NotNull String first, Operation op, @NotNull String second) {
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
