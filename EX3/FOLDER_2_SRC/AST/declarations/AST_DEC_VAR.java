package ast.declarations;

import utils.NotNull;

public abstract class AST_DEC_VAR extends AST_DEC {
    public AST_DEC_VAR(@NotNull String type,@NotNull String name) {
        super(type, name);
    }

    @NotNull
    @Override
    protected String name() {
        return "Unknown var Dec";
    }
}