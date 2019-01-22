package ir.functions;

import ir.IRAssignmentCommand;
import utils.NotNull;

public class IRPopCommand extends IRAssignmentCommand {
    public IRPopCommand(@NotNull String dest) {
        super("pop var", dest);
    }

    @Override
    public String toString() {
        return String.format("pop %s", dest);
    }
}
