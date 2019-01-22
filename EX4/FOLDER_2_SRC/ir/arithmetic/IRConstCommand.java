package ir.arithmetic;

import ir.IRAssignmentCommand;
import utils.NotNull;

public class IRConstCommand extends IRAssignmentCommand  {
    private final int value;
    public IRConstCommand(@NotNull String dest, int value) {
        super("var := const", dest);
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s := %d", dest, value);
    }
}
