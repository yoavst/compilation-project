package ir.functions;

import ir.IRCommand;
import ir.registers.Register;
import utils.NotNull;

public class IRPushCommand extends IRCommand {
    @NotNull
    private final Register source;
    public IRPushCommand(@NotNull Register source) {
        super("push var");
        this.source = source;
    }

    @Override
    public String toString() {
        return String.format("push %s", source);
    }
}
