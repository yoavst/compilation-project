package ast.variables;

import ast.AST_Node;
import utils.NotNull;

public abstract class AST_VAR extends AST_Node {
    @NotNull
    @Override
    protected String name() {
        return "unknown Var";
    }
}
