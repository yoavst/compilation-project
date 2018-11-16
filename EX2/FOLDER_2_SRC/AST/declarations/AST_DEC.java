package ast.declarations;

import ast.AST_Node;

public abstract class AST_DEC extends AST_Node {
    public String type;
    public String name;

    public AST_DEC(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    protected String name() {
        return "Unknown DEC";
    }
}