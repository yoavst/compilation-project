package ir.arithmetic;

import ir.IRAssignmentCommand;
import ir.Register;
import utils.NotNull;

public class IRConstCommand extends IRAssignmentCommand  {
    private final int value;
    public IRConstCommand(@NotNull Register dest, int value) {
        super("var := const", dest);
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("%s := %d", dest, value);
    }
}
