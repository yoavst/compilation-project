package ir.memory;

import ir.IRAssignmentCommand;
import ir.registers.Register;
import utils.NotNull;

public class IRReferenceCommand extends IRAssignmentCommand {
    @NotNull
    private final String variable;
    public IRReferenceCommand(@NotNull Register dest, @NotNull String variable) {
        super("var1 := &variable", dest);
        this.variable = variable;
    }

    @Override
    public String toString() {
        return String.format("%s := &%s", dest, variable);
    }
}
