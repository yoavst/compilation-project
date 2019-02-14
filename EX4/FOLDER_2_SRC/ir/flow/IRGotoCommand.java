package ir.flow;

import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRGotoCommand extends IRFlowCommand {
    public IRGotoCommand(@NotNull IRLabel label) {
        super("goto label", label);
    }

    @Override
    public Set<Register> getDependencies() {
        return setOf();
    }

    @Override
    public Set<Register> getInvalidates() {
        return setOf();
    }

    @Override
    public String toString() {
        return String.format("goto %s", label);
    }
}
