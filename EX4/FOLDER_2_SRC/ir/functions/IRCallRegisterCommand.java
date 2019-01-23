package ir.functions;

import ir.IRCommand;
import ir.Register;
import ir.flow.IRLabel;
import utils.NotNull;

public class IRCallRegisterCommand extends IRCommand {
    @NotNull
    private final Register function;
    public IRCallRegisterCommand(@NotNull Register function) {
        super("call *var1");
        this.function = function;
    }

    @Override
    public String toString() {
        return String.format("call *%s", function);
    }
}
