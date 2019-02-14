package ir.functions;

import ir.IRAssignmentCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRPopCommand extends IRAssignmentCommand {
    public IRPopCommand(@NotNull Register dest) {
        super("pop var", dest);
    }

    @Override
    public Set<Register> getDependencies() {
        return setOf();
    }

    @Override
    public Set<Register> getInvalidates() {
        return setOf(dest);
    }

    @Override
    public String toString() {
        return String.format("pop %s", dest);
    }
}
