package ast.expressions.func;

import ast.expressions.AST_EXP;

import java.util.List;

public class AST_CALLABLE_VAR_CONT_FUNC extends AST_CALLABLE_VAR_CONT {
    List<AST_EXP> expressions;

    public AST_CALLABLE_VAR_CONT_FUNC(List<AST_EXP> expressions) {
        super(null);
        this.expressions = expressions;
    }
}
