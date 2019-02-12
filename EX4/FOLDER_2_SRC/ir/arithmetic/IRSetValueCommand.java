package ir.arithmetic;

import ir.IRAssignmentCommand;
import ir.registers.Register;
import utils.NotNull;

public class IRSetValueCommand extends IRAssignmentCommand  {
    @NotNull
    private final Register source;
    public IRSetValueCommand(@NotNull Register dest, @NotNull Register source) {
        super("var1 := var2", dest);
        this.source = source;
    }

    @Override
    public String toString() {
        return String.format("%s := %s", dest, source);
    }
}
