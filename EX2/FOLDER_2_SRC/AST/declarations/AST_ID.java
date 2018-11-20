package ast.declarations;

import ast.AST_Node;
import utils.NotNull;

public class AST_ID extends AST_Node {
    @NotNull
    public String type;
    @NotNull
    public String name;

    public AST_ID(@NotNull String type, @NotNull String name) {
        this.type = type;
        this.name = name;
    }

    @NotNull
    @Override
    protected String name() {
        return "[" + type + " " + name + "]";
    }
}
