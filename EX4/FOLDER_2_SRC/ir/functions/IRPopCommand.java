package ir.functions;

import ir.IRAssignmentCommand;
import ir.Register;
import utils.NotNull;

public class IRPopCommand extends IRAssignmentCommand {
    public IRPopCommand(@NotNull Register dest) {
        super("pop var", dest);
    }

    @Override
    public String toString() {
        return String.format("pop %s", dest);
    }
}
