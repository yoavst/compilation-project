package utils.errors;

import ast.AST_Node;
import utils.NotNull;

public class ParameterMismatchFunctionCallException extends FunctionCallException {
    public ParameterMismatchFunctionCallException(@NotNull AST_Node node, @NotNull String error) {
        super(node, error);
    }
}
