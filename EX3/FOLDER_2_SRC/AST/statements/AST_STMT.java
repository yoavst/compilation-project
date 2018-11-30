package ast.statements;

import ast.AST_Node;
import utils.NotNull;

public abstract class AST_STMT extends AST_Node {
    @NotNull
    @Override
    protected String name() {
        return "unknown Stmt";
    }

    @Override
    public boolean errorReportable() {
        return true;
    }
}
