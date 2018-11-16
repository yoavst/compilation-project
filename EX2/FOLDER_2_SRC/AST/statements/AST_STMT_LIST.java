package ast.statements;

import ast.AST_Node;

public class AST_STMT_LIST extends AST_Node {
    public AST_STMT head;
    public AST_STMT_LIST tail;

    public AST_STMT_LIST(AST_STMT head, AST_STMT_LIST tail) {
        this.head = head;
        this.tail = tail;
    }

    @Override
    protected String name() {
        return "Stmt list";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(head);
        printAndEdge(tail);
    }
}
