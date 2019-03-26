package utils.errors;

import ast.AST_Node;
import utils.NotNull;

public class UndefinedFunctionCallException extends FunctionCallException {
    public String functionName;
    public UndefinedFunctionCallException(@NotNull AST_Node node, @NotNull String functionName) {
        super(node, "Trying to call non-existent function: " + functionName + "(...)");
        this.functionName = functionName;
    }
}
