package ir.functions;

import ir.IRCommand;
import utils.NotNull;

public class IRPushCommand extends IRCommand {
    @NotNull
    private final String source;
    public IRPushCommand(@NotNull String source) {
        super("push var");
        this.source = source;
    }

    @Override
    public String toString() {
        return String.format("push %s", source);
    }
}
