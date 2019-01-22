package ast.expressions.func;

import ast.expressions.AST_EXP;
import ast.expressions.AST_EXP_FUNC_CALL;
import ast.expressions.AST_EXP_VAR;
import ast.variables.AST_VAR;
import ast.variables.AST_VAR_FIELD;
import ast.variables.AST_VAR_SIMPLE;
import ast.variables.AST_VAR_SUBSCRIPT;
import utils.NotNull;
import utils.Nullable;

public abstract class AST_CALLABLE_VAR_CONT {
    @Nullable
    public AST_CALLABLE_VAR_CONT continuation;

    public int lineNumber;

    public AST_CALLABLE_VAR_CONT(@Nullable AST_CALLABLE_VAR_CONT continuation) {
        this.continuation = continuation;
    }

    /**
     * Convert a continuation into a normal chain of [AST_EXP].
     */
    public static AST_EXP create(String id, @NotNull AST_CALLABLE_VAR_CONT continuation) {
        AST_VAR current = new AST_VAR_SIMPLE(id);
        // all parts of the exp shares the same line number as the first one,
        // as discussed here: http://moodle.tau.ac.il/mod/forum/discuss.php?d=19272#p26383
        int lineNumber = continuation.lineNumber;
        current.lineNumber = lineNumber;

        while (continuation != null) {
            if (continuation instanceof AST_CALLABLE_VAR_CONT_FUNC) {
                assert continuation.continuation == null;
                AST_EXP returnValue;
                if (current instanceof AST_VAR_SIMPLE) {
                    returnValue = new AST_EXP_FUNC_CALL(((AST_VAR_SIMPLE) current).name, ((AST_CALLABLE_VAR_CONT_FUNC) continuation).expressions);
                } else {
                    assert current instanceof AST_VAR_FIELD;
                    returnValue = new AST_EXP_FUNC_CALL(((AST_VAR_FIELD) current).fieldName, ((AST_VAR_FIELD) current).var, ((AST_CALLABLE_VAR_CONT_FUNC) continuation).expressions);
                }
                returnValue.lineNumber = lineNumber;
                return returnValue;
            } else {
                if (continuation instanceof AST_CALLABLE_VAR_CONT_FIELD) {
                    current = new AST_VAR_FIELD(current, ((AST_CALLABLE_VAR_CONT_FIELD) continuation).fieldName);
                } else {
                    current = new AST_VAR_SUBSCRIPT(current, ((AST_CALLABLE_VAR_CONT_SUBSCRIPT) continuation).exp);
                }
                current.lineNumber = lineNumber;
            }
            continuation = continuation.continuation;
        }
        return new AST_EXP_VAR(current);
    }

    public static AST_EXP createId(String id) {
        return new AST_EXP_VAR(new AST_VAR_SIMPLE(id));
    }
}