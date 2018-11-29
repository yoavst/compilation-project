package ast.expressions;

import utils.NotNull;

public class AST_EXP_NIL extends AST_EXP {
    @NotNull
    @Override
    protected String name() {
        return "NIL";
    }
}
