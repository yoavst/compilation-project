package ir.memory;

import ir.IRAssignmentCommand;
import ir.Register;
import utils.NotNull;

public class IRLoadPreallocatedCommand extends IRAssignmentCommand {
    private final int resourceId;
    public IRLoadPreallocatedCommand(@NotNull Register dest, int resourceId) {
        super("var1 := res[id]", dest);
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return String.format("%s := res[%d]", dest, resourceId);
    }
}
