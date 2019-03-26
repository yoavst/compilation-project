package utils.errors;

import ast.AST_Node;
import utils.NotNull;

public class SemanticException extends Exception {
    @NotNull
    private final AST_Node node;
    @NotNull
    private final String reason;

    public SemanticException(@NotNull AST_Node node, @NotNull String reason) {
        super(reason);
        this.node = node;
        this.reason = reason;
    }

    @NotNull
    public AST_Node getNode() {
        return node;
    }

    @NotNull
    public String getReason() {
        return reason;
    }
}
