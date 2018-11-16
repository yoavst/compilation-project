package ast.expressions;

public class AST_NEW_EXP_JUSTID extends AST_NEW_EXP {
    public String className;

    public AST_NEW_EXP_JUSTID(String className) {
        this.className = className;
    }

    @Override
    protected String name() {
        return "new " + className;
    }
}
