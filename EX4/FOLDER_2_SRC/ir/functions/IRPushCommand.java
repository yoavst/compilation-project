package ir.functions;

import ir.IRCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRPushCommand extends IRCommand {
    @NotNull
    private final Register source;
    public IRPushCommand(@NotNull Register source) {
        super("push var");
        this.source = source;
    }

    @Override
    public Set<Register> getDependencies() {
        return setOf(source);
    }

    @Override
    public Set<Register> getInvalidates() {
        return setOf();
    }

    @Override
    public String toString() {
        return String.format("push %s", source);
    }
}
