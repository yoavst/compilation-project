package ir.commands.functions;

import ir.commands.IRCommand;
import ir.registers.Register;
import utils.NotNull;

import static utils.Utils.setOf;

public class IRPushConstCommand extends IRCommand {
    public final int constant;

    public IRPushConstCommand( int constant) {
        super("push const");
        this.constant = constant;
    }

    @Override
    public String toString() {
        return String.format("push %d", constant);
    }
}
