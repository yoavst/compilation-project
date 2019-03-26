package ir.commands.functions;

import ir.commands.IRCommand;

public class IRFunctionInfo extends IRCommand {
    public String name;
    public int numberOfParameters;
    public int numberOfLocals;

    public IRFunctionInfo(String name, int numberOfParameters, int numberOfLocals) {
        super("// params: count, locals: count");
        this.name = name;
        this.numberOfParameters = numberOfParameters;
        this.numberOfLocals = numberOfLocals;
    }

    @Override
    public String toString() {
        return String.format("// name: %s, params: %d, locals: %d", name, numberOfParameters, numberOfLocals);
    }
}
