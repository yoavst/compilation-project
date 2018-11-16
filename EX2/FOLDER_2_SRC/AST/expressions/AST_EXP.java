package ast.expressions;

import ast.AST_Node;

public abstract class AST_EXP extends AST_Node {
    @Override
    protected String name() {
        return "Unknown Exp";
    }
}