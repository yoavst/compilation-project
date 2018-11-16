package ast.expressions;

import ast.statements.AST_STMT;

public abstract class AST_NEW_EXP extends AST_STMT {
    @Override
    protected String name() {
        return "Unknown new Exp";
    }
}
