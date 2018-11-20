package ast.expressions.func;

import ast.expressions.AST_EXP;
import utils.NotNull;

import java.util.Collections;
import java.util.List;

public class AST_CALLABLE_VAR_CONT_FUNC extends AST_CALLABLE_VAR_CONT {
    @NotNull
    List<AST_EXP> expressions;

    public AST_CALLABLE_VAR_CONT_FUNC(@NotNull List<AST_EXP> expressions) {
        super(null);
        this.expressions = expressions;
    }

    public AST_CALLABLE_VAR_CONT_FUNC() {
        this(Collections.emptyList());
    }
}
