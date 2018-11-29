package ast.declarations;

import ast.expressions.AST_EXP;
import utils.NotNull;
import utils.Nullable;

public class AST_DEC_VAR_EXP extends AST_DEC_VAR {
    @Nullable
    public AST_EXP exp;

    public AST_DEC_VAR_EXP(@NotNull String type,@NotNull String name) {
        super(type, name);
    }

    public AST_DEC_VAR_EXP(@NotNull String type, @NotNull String name, @Nullable AST_EXP exp) {
        super(type, name);
        this.exp = exp;
    }

    @NotNull
    @Override
    protected String name() {
        return "var " + type + " " + name;
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(exp);
    }
}