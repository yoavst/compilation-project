package ir.functions;

import ir.IRCommand;

public class IRReturnCommand extends IRCommand {
    public IRReturnCommand() {
        super("return");
    }

    @Override
    public String toString() {
        return "return";
    }
}
