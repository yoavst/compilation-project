package ast.expressions;

import utils.NotNull;

public class AST_EXP_BINOP extends AST_EXP {
    @NotNull
    public Op op;
    @NotNull
    public AST_EXP left;
    @NotNull
    public AST_EXP right;


    public AST_EXP_BINOP(@NotNull AST_EXP left, @NotNull AST_EXP right, @NotNull Op op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @NotNull
    @Override
    protected String name() {
        return op.text;
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(left);
        printAndEdge(right);
    }

    public enum Op {
        Plus("+"), Minus("-"), Times("*"), Divide("/"), LT("<"), GT(">"), EQ("=");
        @NotNull
        String text;

        Op(@NotNull String text) {
            this.text = text;
        }
    }
}
