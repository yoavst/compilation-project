package ir.commands;

public class IRNopCommand extends IRCommand {
    public IRNopCommand() {
        super("nop");
    }

    @Override
    public String toString() {
        return "nop";
    }

    @Override
    public boolean canBeOptimized() {
        return true;
    }
}
