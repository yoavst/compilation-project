package ast.expressions;

import ast.AST_Node;
import utils.NotNull;

public abstract class AST_EXP extends AST_Node {
    @NotNull
    @Override
    protected String name() {
        return "Unknown Exp";
    }
}