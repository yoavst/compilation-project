package utils;

import ast.AST_Node;

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

}
