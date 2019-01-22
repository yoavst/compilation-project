package ast.expressions;

import types.builtins.TypeNil;
import utils.NotNull;

public class AST_EXP_NIL extends AST_EXP_CONSTANT {
    public AST_EXP_NIL() {
        super(TypeNil.instance);
    }

    @NotNull
    @Override
    protected String name() {
        return "NIL";
    }
}
