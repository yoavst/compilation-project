package ast.declarations;

import utils.NotNull;

public class AST_DEC_ARRAY extends AST_DEC {
    public AST_DEC_ARRAY(String type, String name) {
        super(type, name);
    }

    @NotNull
    @Override
    protected String name() {
        return "Arr: " + type + "[] " + name;
    }
}