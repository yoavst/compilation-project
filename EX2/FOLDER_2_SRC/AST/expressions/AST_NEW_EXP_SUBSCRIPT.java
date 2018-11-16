package ast.expressions;

public class AST_NEW_EXP_SUBSCRIPT extends AST_NEW_EXP {
    public String className;
    public AST_EXP subscript;

    public AST_NEW_EXP_SUBSCRIPT(String className, AST_EXP subscript) {
        this.className = className;
        this.subscript = subscript;
    }

    @Override
    protected String name() {
        return "new " + className + "[...]";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(subscript);
    }
}
