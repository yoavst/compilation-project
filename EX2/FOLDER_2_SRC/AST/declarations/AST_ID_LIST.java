package ast.declarations;

import ast.AST_Node;

public class AST_ID_LIST extends AST_Node {
    public String type;
    public String name;
    public AST_ID_LIST tail;

    public AST_ID_LIST(String type, String name, AST_ID_LIST tail) {
        this.type = type;
        this.name = name;
        this.tail = tail;
    }

    public AST_ID_LIST(String type, String name) {
        this(type, name, null);
    }

    @Override
    protected String name() {
        return "param [" + type + " " + name + "]";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(tail);
    }
}
