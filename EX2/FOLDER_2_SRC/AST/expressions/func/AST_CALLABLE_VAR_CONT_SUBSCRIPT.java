package ast.expressions.func;

import ast.expressions.AST_EXP;

public class AST_CALLABLE_VAR_CONT_SUBSCRIPT extends AST_CALLABLE_VAR_CONT {
    AST_EXP exp;

    public AST_CALLABLE_VAR_CONT_SUBSCRIPT(AST_CALLABLE_VAR_CONT continuation, AST_EXP exp) {
        super(continuation);
        this.exp = exp;
    }
}
