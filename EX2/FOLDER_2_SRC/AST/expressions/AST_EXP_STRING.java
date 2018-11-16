package ast.expressions;

public class AST_EXP_STRING extends AST_EXP {
    public String value;

    public AST_EXP_STRING(String value) {
        this.value = value;
    }

    @Override
    protected String name() {
        if (value.length() > 5)
            return "STR(" + value.substring(0, 5) + "...)";
        return "STR(" + value + ")";
    }
}
