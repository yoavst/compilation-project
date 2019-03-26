package utils.errors;

import ast.AST_Node;
import utils.NotNull;

public abstract class FunctionCallException extends SemanticException {
    public FunctionCallException(@NotNull AST_Node node, @NotNull String reason) {
        super(node, reason);
    }
}
