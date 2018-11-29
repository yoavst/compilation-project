package ast.expressions.func;

import ast.expressions.AST_EXP;
import utils.NotNull;
import utils.Nullable;

public class AST_CALLABLE_VAR_CONT_SUBSCRIPT extends AST_CALLABLE_VAR_CONT {
    @NotNull
    AST_EXP exp;

    public AST_CALLABLE_VAR_CONT_SUBSCRIPT(@Nullable AST_CALLABLE_VAR_CONT continuation, @NotNull AST_EXP exp) {
        super(continuation);
        this.exp = exp;
    }
}
