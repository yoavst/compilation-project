package ast.expressions;

import utils.NotNull;

public class AST_NEW_EXP_SUBSCRIPT extends AST_NEW_EXP {
    @NotNull
    public AST_EXP subscript;

    public AST_NEW_EXP_SUBSCRIPT(String className, @NotNull AST_EXP subscript) {
        super(className);
        this.subscript = subscript;
    }

    @NotNull
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
