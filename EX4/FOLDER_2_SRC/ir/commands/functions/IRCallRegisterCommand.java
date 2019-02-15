package ir.commands.functions;

import ir.commands.IRCommand;
import ir.registers.Register;
import utils.NotNull;

import java.util.Set;

import static utils.Utils.setOf;

public class IRCallRegisterCommand extends IRCommand {
    @NotNull
    public final Register function;

    public IRCallRegisterCommand(@NotNull Register function) {
        super("call *var1");
        this.function = function;
    }

    @Override
    public Set<Register> getDependencies() {
        return setOf(function);
    }

    @Override
    public String toString() {
        return String.format("call *%s", function);
    }
}
