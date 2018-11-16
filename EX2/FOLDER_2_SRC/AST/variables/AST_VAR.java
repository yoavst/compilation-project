package ast.variables;

import ast.AST_Node;

public abstract class AST_VAR extends AST_Node {
    @Override
    protected String name() {
        return "unknown Var";
    }
}
