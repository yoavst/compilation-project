package ir.functions;

import ir.IRCommand;
import utils.NotNull;

public class IRReturnConstCommand extends IRCommand {
    private final int value;
    public IRReturnConstCommand(int value) {
        super("return var");
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("return %d", value);
    }
}
