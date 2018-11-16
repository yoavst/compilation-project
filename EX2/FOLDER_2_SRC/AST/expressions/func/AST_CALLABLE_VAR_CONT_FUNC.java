package ast.expressions.func;

import ast.expressions.AST_EXP_LIST;

public class AST_CALLABLE_VAR_CONT_FUNC extends AST_CALLABLE_VAR_CONT {
    AST_EXP_LIST expList;

    public AST_CALLABLE_VAR_CONT_FUNC(AST_EXP_LIST expList) {
        super(null);
        this.expList = expList;
    }
}
