package ast.declarations;

import ast.AST_Node;
import utils.NotNull;
import utils.Nullable;

public abstract class AST_DEC extends AST_Node {
    @Nullable
    public String type;
    @NotNull
    public String name;

    public AST_DEC(@Nullable String type, @NotNull String name) {
        this.type = type;
        this.name = name;
    }

    @NotNull
    @Override
    protected String name() {
        return "Unknown DEC";
    }
}