package ast.variables;

import utils.NotNull;

public class AST_VAR_SIMPLE extends AST_VAR {
    @NotNull
    public String name;

    public AST_VAR_SIMPLE(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    @Override
    protected String name() {
        return "VAR(" + name + ")";
    }
}
