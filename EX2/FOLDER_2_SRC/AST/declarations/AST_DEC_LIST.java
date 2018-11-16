package ast.declarations;

import ast.AST_Node;

public class AST_DEC_LIST extends AST_Node {
    public AST_DEC head;
    public AST_DEC_LIST tail;

    public AST_DEC_LIST(AST_DEC head, AST_DEC_LIST tail) {
        this.head = head;
        this.tail = tail;
    }

    @Override
    protected String name() {
        return "Dec list";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(head);
        printAndEdge(tail);
    }


}
