package ast.expressions.func;

public class AST_CALLABLE_VAR_CONT_FIELD extends AST_CALLABLE_VAR_CONT {
    public String fieldName;

    public AST_CALLABLE_VAR_CONT_FIELD(AST_CALLABLE_VAR_CONT continuation, String fieldName) {
        super(continuation);
        this.fieldName = fieldName;
    }
}
