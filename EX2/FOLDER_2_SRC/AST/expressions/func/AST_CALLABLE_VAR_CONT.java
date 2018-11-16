package ast.expressions.func;

import ast.expressions.AST_EXP;
import ast.expressions.AST_EXP_FUNC_CALL;
import ast.expressions.AST_EXP_VAR;
import ast.variables.AST_VAR;
import ast.variables.AST_VAR_FIELD;
import ast.variables.AST_VAR_SIMPLE;
import ast.variables.AST_VAR_SUBSCRIPT;

public abstract class AST_CALLABLE_VAR_CONT {
    public AST_CALLABLE_VAR_CONT continuation;

    public AST_CALLABLE_VAR_CONT(AST_CALLABLE_VAR_CONT continuation) {
        this.continuation = continuation;
    }

    public static AST_EXP create(String id, AST_CALLABLE_VAR_CONT continuation) {
        AST_VAR current = new AST_VAR_SIMPLE(id);
        while (continuation != null) {
            if (continuation instanceof AST_CALLABLE_VAR_CONT_FIELD) {
                current = new AST_VAR_FIELD(current, ((AST_CALLABLE_VAR_CONT_FIELD) continuation).fieldName);
            } else if (continuation instanceof AST_CALLABLE_VAR_CONT_SUBSCRIPT) {
                current = new AST_VAR_SUBSCRIPT(current, ((AST_CALLABLE_VAR_CONT_SUBSCRIPT) continuation).exp);
            } else {
                assert continuation.continuation == null;
                if (current instanceof AST_VAR_SIMPLE) {
                    return new AST_EXP_FUNC_CALL(((AST_VAR_SIMPLE) current).name, ((AST_CALLABLE_VAR_CONT_FUNC) continuation).expList);
                } else {
                    assert current instanceof AST_VAR_FIELD;
                    return new AST_EXP_FUNC_CALL(((AST_VAR_FIELD) current).fieldName, ((AST_VAR_FIELD) current).var, ((AST_CALLABLE_VAR_CONT_FUNC) continuation).expList);
                }
            }
            continuation = continuation.continuation;
        }
        return new AST_EXP_VAR(current);
    }
}