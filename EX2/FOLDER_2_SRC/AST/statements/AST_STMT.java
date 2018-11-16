package ast.statements;

import ast.AST_Node;

public abstract class AST_STMT extends AST_Node {
    @Override
    protected String name() {
        return "unknown Stmt";
    }
}
