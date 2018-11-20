package ast;

import ast.declarations.AST_DEC;

import java.util.List;

public class AST_PROGRAM extends AST_Node {
    public List<AST_DEC> declarations;

    public AST_PROGRAM(List<AST_DEC> declarations) {
        this.declarations = declarations;
    }



    @Override
    protected String name() {
        return "Program";
    }

    @Override
    public void printMe() {
        super.printMe();
        for (AST_DEC declaration : declarations) {
            printAndEdge(declaration);
        }
    }
}
