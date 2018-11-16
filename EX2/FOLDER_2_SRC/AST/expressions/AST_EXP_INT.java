package ast.expressions;

public class AST_EXP_INT extends AST_EXP {
    public int value;

    public AST_EXP_INT(int value) {
        this.value = value;
    }

    @Override
    protected String name() {
        return "INT(" + value + ")";
    }
}
