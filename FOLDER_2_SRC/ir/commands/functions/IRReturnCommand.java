package ir.commands.functions;

import ir.commands.IRCommand;

public class IRReturnCommand extends IRCommand {
    public IRReturnCommand() {
        super("return");
    }

    @Override
    public String toString() {
        return "return";
    }
}
