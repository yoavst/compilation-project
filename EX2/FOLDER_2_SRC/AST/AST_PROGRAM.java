package ast;

import ast.declarations.AST_DEC_LIST;

public class AST_PROGRAM extends AST_Node {
    public AST_DEC_LIST list;

    public AST_PROGRAM(AST_DEC_LIST list) {
        this.list = list;
    }

    @Override
    protected String name() {
        return "Program";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(list);
    }
}
