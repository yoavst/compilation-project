package utils.errors;

import ast.AST_Node;
import utils.NotNull;

public class FunctionCallWrappingException extends FunctionCallException {
    public FunctionCallWrappingException(@NotNull AST_Node node,@NotNull FunctionCallException wrappedException) {
        super(node, "Wrapping function call exception");
        addSuppressed(wrappedException);
    }
}
