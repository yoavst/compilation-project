package ast.expressions;

public class AST_EXP_BINOP extends AST_EXP {
    public Op op;
    public AST_EXP left;
    public AST_EXP right;


    public AST_EXP_BINOP(AST_EXP left, AST_EXP right, Op op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

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
        String text;

        Op(String text) {
            this.text = text;
        }
    }
}
