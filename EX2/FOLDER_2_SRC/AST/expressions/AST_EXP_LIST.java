package ast.expressions;

public class AST_EXP_LIST extends AST_EXP {
    public AST_EXP head;
    public AST_EXP_LIST tail;

    public AST_EXP_LIST(AST_EXP head, AST_EXP_LIST tail) {
        this.head = head;
        this.tail = tail;
    }

    @Override
    protected String name() {
        return "Exp list";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(head);
        printAndEdge(tail);
    }
}
