package ir.functions;

import ir.IRCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRCallRegisterCommand extends IRCommand {
    @NotNull
    private final Register function;
    public IRCallRegisterCommand(@NotNull Register function) {
        super("call *var1");
        this.function = function;
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
        return String.format("call *%s", function);
    }
}
