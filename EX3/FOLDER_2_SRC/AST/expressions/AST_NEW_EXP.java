package ast.expressions;

import ast.statements.AST_STMT;
import utils.NotNull;

public class AST_NEW_EXP extends AST_STMT {
    @NotNull
    public String className;

    public AST_NEW_EXP(@NotNull String className) {
        this.className = className;
    }

    @NotNull
    @Override
    protected String name() {
        return "new " + className;
    }
}
