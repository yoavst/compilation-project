package ir.arithmetic;

import ir.IRCommand;

public class IRNopCommand extends IRCommand {
    public IRNopCommand() {
        super("nop");
    }

    @Override
    public String toString() {
        return "nop";
    }
}
