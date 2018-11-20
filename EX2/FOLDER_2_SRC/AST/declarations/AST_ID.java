package ast.declarations;

import ast.AST_Node;

public class AST_ID extends AST_Node {
    public String type;
    public String name;

    public AST_ID(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    protected String name() {
        return "[" + type + " " + name + "]";
    }
}
