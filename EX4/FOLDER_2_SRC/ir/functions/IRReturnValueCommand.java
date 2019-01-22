package ir.functions;

import ir.IRCommand;
import utils.NotNull;

public class IRReturnValueCommand extends IRCommand {
    @NotNull
    private final String value;
    public IRReturnValueCommand(@NotNull String value) {
        super("return var");
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("return %s", value);
    }
}
