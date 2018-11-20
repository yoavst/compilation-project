package ast.expressions.func;

import utils.NotNull;
import utils.Nullable;

public class AST_CALLABLE_VAR_CONT_FIELD extends AST_CALLABLE_VAR_CONT {
    @NotNull
    public String fieldName;

    public AST_CALLABLE_VAR_CONT_FIELD(@Nullable AST_CALLABLE_VAR_CONT continuation, @NotNull String fieldName) {
        super(continuation);
        this.fieldName = fieldName;
    }
}
