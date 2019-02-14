package ir.functions;

import ir.IRCommand;
import ir.flow.IRLabel;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRCallCommand extends IRCommand {
    @NotNull
    private final IRLabel label;
    public IRCallCommand(@NotNull IRLabel label) {
        super("call label");
        this.label = label;
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
        return String.format("call %s", label);
    }
}
