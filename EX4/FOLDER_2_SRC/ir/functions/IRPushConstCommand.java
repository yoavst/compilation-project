package ir.functions;

import ir.IRCommand;
import utils.NotNull;

public class IRPushConstCommand extends IRCommand {
    private final int operand;
    public IRPushConstCommand(int operand) {
        super("push const");
        this.operand = operand;
    }

    @Override
    public String toString() {
        return String.format("push %d", operand);
    }
}
